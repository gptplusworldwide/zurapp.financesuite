
package com.financesuite.zsurapp.transaction.service;

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

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.repository.CategoryRepository;

import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.entity.TransactionType;
import com.financesuite.zsurapp.transaction.repository.TransactionRepository;

import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class TransactionService {

	private final TransactionRepository repository;
	private final AccountRepository accountRepository;
	private final CategoryRepository categoryRepository;
	private final WorkspaceService workspaceService;

	public TransactionService(TransactionRepository repository, AccountRepository accountRepository,
			CategoryRepository categoryRepository, WorkspaceService workspaceService) {

		this.repository = repository;
		this.accountRepository = accountRepository;
		this.categoryRepository = categoryRepository;
		this.workspaceService = workspaceService;
	}

	@Transactional
	public Transaction create(UUID workspaceId, UUID accountId, UUID categoryId, TransactionType type,
			BigDecimal amount, String description, Instant occurredAt, UUID ownerId) {

		// 1. Verificar propiedad del Workspace.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		// 2. Verificar que la cuenta pertenece al Workspace.
		Account account = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

		// 3. Verificar que la categoria pertenece al Workspace.
		Category category = categoryRepository.findByIdAndWorkspaceId(categoryId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

		// 4. Validar los datos financieros.
		if (type == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es obligatorio");
		}

		if (amount == null || amount.signum() <= 0 || amount.scale() > 4 || amount.precision() > 19) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Importe invalido");
		}

		if (occurredAt == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha es obligatoria");
		}

		if (description != null && description.trim().length() > 500) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripcion supera los 500 caracteres");
		}

		// 5. El tipo debe coincidir con la categoria.
		if (!category.getType().name().equals(type.name())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo no coincide con la categoria");
		}

		// 6. Construir y guardar la transaccion.
		Transaction transaction = new Transaction(workspaceId, account.getId(), category.getId(), type, amount,
				description, occurredAt);

		return repository.save(transaction);
	}

	@Transactional(readOnly = true)
	public List<Transaction> findAllByWorkspace(UUID workspaceId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findAllByWorkspaceIdOrderByOccurredAtDesc(workspaceId);
	}

	@Transactional(readOnly = true)
	public Transaction findById(UUID workspaceId, UUID transactionId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findByIdAndWorkspaceId(transactionId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaccion no encontrada"));
	}
}
