package io.dot.lorraine.dsl

import io.dot.lorraine.models.BackoffLorrainePolicy
import io.dot.lorraine.work.DataDefinition
import io.dot.lorraine.work.LorraineData
import io.dot.lorraine.work.workData
import kotlin.time.Duration

/**
 * Class to make a request to work
 */
@ConsistentCopyVisibility
data class LorraineRequest internal constructor(
    val identifier: String,
    val constraints: LorraineConstraints,
    val tags: Set<String>,
    val inputData: LorraineData?,
    val backOffPolicy: BackoffLorrainePolicy?
)

open class LorraineRequestDefinition internal constructor() {
    private val tags: MutableSet<String> = mutableSetOf()

    private var constraints: LorraineConstraints = LorraineConstraints.NONE
    private var inputData: LorraineData? = null
    private var backoffLorrainePolicy: BackoffLorrainePolicy? = null

    var identifier: String? = null

    fun addTag(tag: String) {
        tags.add(tag)
    }

    fun backOffPolicy(duration: Duration, policy: BackoffLorrainePolicy.Policy) {
        backoffLorrainePolicy = BackoffLorrainePolicy(
            duration = duration,
            policy = policy
        )
    }

    fun data(block: DataDefinition.() -> Unit) {
        inputData = workData(block)
    }

    fun constraints(block: LorraineConstraintsDefinition.() -> Unit) {
        val definition = LorraineConstraintsDefinition().apply(block)

        constraints = definition.build()
    }

    internal fun build(): LorraineRequest {
        val identifier = requireNotNull(identifier) { "Identifier must not be null" }

        return LorraineRequest(
            constraints = constraints,
            tags = tags,
            inputData = inputData,
            identifier = identifier,
            backOffPolicy = backoffLorrainePolicy
        )
    }

}

/**
 *
 */
fun lorraineRequest(block: LorraineRequestDefinition.() -> Unit): LorraineRequest {
    val definition = LorraineRequestDefinition()

    return definition.apply(block)
        .build()
}

