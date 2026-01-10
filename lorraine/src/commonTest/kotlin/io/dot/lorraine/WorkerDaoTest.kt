package io.dot.lorraine

import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.db.entity.ConstraintEntity
import io.dot.lorraine.db.entity.WorkerEntity
import io.dot.lorraine.models.LorraineInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkerDaoTest {

    private lateinit var dao: WorkerDao

    @BeforeTest
    fun before() {
        val db = createDatabase()

        dao = db.workerDao()
    }

    @Test
    fun `test insert`() = runTest {
        val entity = WorkerEntity(
            uuid = "uuid",
            queueId = "queueId",
            identifier = "identifier",
            inputData = null,
            outputData = null,
            constraints = ConstraintEntity(
                requireNetwork = false
            ),
            workerDependencies = emptySet(),
            state = LorraineInfo.State.SUCCEEDED,
            tags = emptySet()
        )

        dao.insert(entity)

        assertEquals(
            expected = entity,
            actual = dao.getWorker(entity.uuid)
        )
    }

}