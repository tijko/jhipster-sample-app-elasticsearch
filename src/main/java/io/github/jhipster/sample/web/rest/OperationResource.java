package io.github.jhipster.sample.web.rest;

import io.github.jhipster.sample.domain.Operation;
import io.github.jhipster.sample.repository.OperationRepository;
import io.github.jhipster.sample.repository.search.OperationSearchRepository;
import io.github.jhipster.sample.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.sample.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link io.github.jhipster.sample.domain.Operation}.
 */
@RestController
@RequestMapping("/api/operations")
@Transactional
public class OperationResource {

    private final Logger log = LoggerFactory.getLogger(OperationResource.class);

    private static final String ENTITY_NAME = "operation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OperationRepository operationRepository;

    private final OperationSearchRepository operationSearchRepository;

    public OperationResource(OperationRepository operationRepository, OperationSearchRepository operationSearchRepository) {
        this.operationRepository = operationRepository;
        this.operationSearchRepository = operationSearchRepository;
    }

    /**
     * {@code POST  /operations} : Create a new operation.
     *
     * @param operation the operation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new operation, or with status {@code 400 (Bad Request)} if the operation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Operation> createOperation(@Valid @RequestBody Operation operation) throws URISyntaxException {
        log.debug("REST request to save Operation : {}", operation);
        if (operation.getId() != null) {
            throw new BadRequestAlertException("A new operation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Operation result = operationRepository.save(operation);
        operationSearchRepository.index(result);
        return ResponseEntity
            .created(new URI("/api/operations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /operations/:id} : Updates an existing operation.
     *
     * @param id the id of the operation to save.
     * @param operation the operation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated operation,
     * or with status {@code 400 (Bad Request)} if the operation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the operation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Operation> updateOperation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Operation operation
    ) throws URISyntaxException {
        log.debug("REST request to update Operation : {}, {}", id, operation);
        if (operation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, operation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!operationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Operation result = operationRepository.save(operation);
        operationSearchRepository.index(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, operation.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /operations/:id} : Partial updates given fields of an existing operation, field will ignore if it is null
     *
     * @param id the id of the operation to save.
     * @param operation the operation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated operation,
     * or with status {@code 400 (Bad Request)} if the operation is not valid,
     * or with status {@code 404 (Not Found)} if the operation is not found,
     * or with status {@code 500 (Internal Server Error)} if the operation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Operation> partialUpdateOperation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Operation operation
    ) throws URISyntaxException {
        log.debug("REST request to partial update Operation partially : {}, {}", id, operation);
        if (operation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, operation.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!operationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Operation> result = operationRepository
            .findById(operation.getId())
            .map(existingOperation -> {
                if (operation.getDate() != null) {
                    existingOperation.setDate(operation.getDate());
                }
                if (operation.getDescription() != null) {
                    existingOperation.setDescription(operation.getDescription());
                }
                if (operation.getAmount() != null) {
                    existingOperation.setAmount(operation.getAmount());
                }

                return existingOperation;
            })
            .map(operationRepository::save)
            .map(savedOperation -> {
                operationSearchRepository.index(savedOperation);
                return savedOperation;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, operation.getId().toString())
        );
    }

    /**
     * {@code GET  /operations} : get all the operations.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of operations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Operation>> getAllOperations(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        log.debug("REST request to get a page of Operations");
        Page<Operation> page;
        if (eagerload) {
            page = operationRepository.findAllWithEagerRelationships(pageable);
        } else {
            page = operationRepository.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /operations/:id} : get the "id" operation.
     *
     * @param id the id of the operation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the operation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Operation> getOperation(@PathVariable("id") Long id) {
        log.debug("REST request to get Operation : {}", id);
        Optional<Operation> operation = operationRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(operation);
    }

    /**
     * {@code DELETE  /operations/:id} : delete the "id" operation.
     *
     * @param id the id of the operation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable("id") Long id) {
        log.debug("REST request to delete Operation : {}", id);
        operationRepository.deleteById(id);
        operationSearchRepository.deleteFromIndexById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /operations/_search?query=:query} : search for the operation corresponding
     * to the query.
     *
     * @param query the query of the operation search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<Operation>> searchOperations(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Operations for query {}", query);
        try {
            Page<Operation> page = operationSearchRepository.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
