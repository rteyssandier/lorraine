package io.dot.lorraine.dsl

import io.dot.lorraine.models.ExistingLorrainePolicy

@ConsistentCopyVisibility
data class LorraineOperation internal constructor(
    val operations: List<Operation>,
    val existingPolicy: ExistingLorrainePolicy
) {

    data class Operation(
        val request: LorraineRequest
    )

}

class LorraineOperationDefinition internal constructor() {

    private val operations: MutableList<LorraineOperation.Operation> = mutableListOf()
    private var constraints: LorraineConstraints? = null

    var existingPolicy: ExistingLorrainePolicy? = null

    fun startWith(request: LorraineRequest) {
        operations.add(0, LorraineOperation.Operation(request = request))
    }

    fun startWith(block: LorraineRequestOperationDefinition.() -> Unit) {
        val request = LorraineRequestOperationDefinition().apply(block)
            .buildOperation()

        operations.add(0, request)
    }

    fun then(request: LorraineRequest) {
        operations.add(LorraineOperation.Operation(request = request))
    }

    fun then(block: LorraineRequestOperationDefinition.() -> Unit) {
        val request = LorraineRequestOperationDefinition().apply(block)
            .buildOperation()

        operations.add(request)
    }

    fun constrainedAll(block: LorraineConstraintsDefinition.() -> Unit) {
        constraints = LorraineConstraintsDefinition().apply(block)
            .build()
    }

    fun build(): LorraineOperation {
        val policy = requireNotNull(existingPolicy) { "Existing policy must no be null" }
        val operations = constraints?.let { constraints ->
            operations.map { operation ->
                operation.copy(
                    request = operation.request
                        .copy(constraints = constraints)
                )
            }
        } ?: operations

        return LorraineOperation(
            operations = operations,
            existingPolicy = policy
        )
    }

}

/**
 * Delete it, type has no purpose for operation
 */
class LorraineRequestOperationDefinition internal constructor() : LorraineRequestDefinition() {

    var type: ExistingLorrainePolicy = ExistingLorrainePolicy.APPEND

    fun buildOperation(): LorraineOperation.Operation {
        return LorraineOperation.Operation(request = build())
    }

}

/**
 * Dsl method to create a [LorraineOperation]
 */
fun lorraineOperation(
    block: LorraineOperationDefinition.() -> Unit
): LorraineOperation {
    val definition = LorraineOperationDefinition().apply(block)

    return definition.build()
}

/**
 * Method to create a [LorraineOperation] from a [LorraineRequest]
 */
infix fun LorraineRequest.then(block: LorraineOperationDefinition.() -> Unit): LorraineOperation {
    val definition = LorraineOperationDefinition()

    definition.startWith(this)

    return definition.apply(block)
        .build()
}