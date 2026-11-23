
package com.financesuite.zsurapp.transfer.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.transfer.entity.Transfer;
import com.financesuite.zsurapp.transfer.repository.TransferRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class TransferService {

	private final TransferRepository repository;
	private final AccountRepository accountRepository;
	private final WorkspaceService workspaceService;

	public TransferService(TransferRepository repository, AccountRepository accountRepository,
			WorkspaceService workspaceService) {

		this.repository = repository;
		this.accountRepository = accountRepository;
		this.workspaceService = workspaceService;
	}

	@Transactional
	public Transfer create(UUID workspaceId, UUID fromAccountId, UUID toAccountId, BigDecimal amount,
			String description, Instant occurredAt, UUID ownerId) {

		// 1. Validar que el Workspace pertenece al usuario.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		// 2. Validar que ambas cuentas fueron informadas.
		if (fromAccountId == null || toAccountId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cuentas son obligatorias");
		}

		// 3. No permitir transferencias a la misma cuenta.
		if (fromAccountId.equals(toAccountId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cuentas deben ser diferentes");
		}

		// 4. Validar importe.
		if (amount == null || amount.signum() <= 0 || amount.scale() > 4 || amount.precision() > 19) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Importe invalido");
		}

		// 5. Validar fecha.
		if (occurredAt == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha es obligatoria");
		}

		// 6. Validar descripcion.
		if (description != null && description.trim().length() > 500) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripcion supera los 500 caracteres");
		}

		// 7. Buscar cuenta de origen dentro del Workspace.
		Account fromAccount = accountRepository.findByIdAndWorkspaceId(fromAccountId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta de origen no encontrada"));

		// 8. Buscar cuenta de destino dentro del Workspace.
		Account toAccount = accountRepository.findByIdAndWorkspaceId(toAccountId, workspaceId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta de destino no encontrada"));

		// 9. Validar que ambas cuentas usan la misma moneda.
		if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cuentas deben utilizar la misma moneda");
		}

		// 10. Crear una unica transferencia.
		Transfer transfer = new Transfer(workspaceId, fromAccount.getId(), toAccount.getId(), amount, description,
				occurredAt);

		// 11. Guardar dentro de la transaccion de BD.
		return repository.save(transfer);
	}

	@Transactional(readOnly = true)
	public List<Transfer> findAllByWorkspace(UUID workspaceId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findAllByWorkspaceIdOrderByOccurredAtDesc(workspaceId);
	}

	@Transactional(readOnly = true)
	public Transfer findById(UUID workspaceId, UUID transferId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findByIdAndWorkspaceId(transferId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));
	}

	/*
	 * 3. ¿Qué valida este servicio? Situación Resultado previsto Workspace de otro
	 * usuario 404 Not Found Cuenta de origen inexistente o ajena 404 Not Found
	 * Cuenta de destino inexistente o ajena 404 Not Found Misma cuenta de origen y
	 * destino 400 Bad Request Cuentas con monedas diferentes 400 Bad Request
	 * Importe cero o negativo 400 Bad Request Importe con más de cuatro decimales
	 * 400 Bad Request Descripción superior a 500 caracteres 400 Bad Request
	 * Transferencia válida Se guarda en PostgreSQL
	 */

}
