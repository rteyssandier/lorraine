@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.dot.lorraine.AndroidPlatform
import io.dot.lorraine.models.LorraineInfo
import io.dot.lorraine.models.workerDao
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

internal class LorraineWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val application = AndroidPlatform.application ?: run {
            Log.e("Lorraine", "LorraineApplication not initialized")
            return Result.retry()
        }
        val dao = application.workerDao

        return runCatching {
            val uuid = id.toKotlinUuid()
            val worker = requireNotNull(dao.getWorker(uuidString = uuid.toString())) {
                "Worker not found"
            }
            val workerDefinition = requireNotNull(application.definitions[worker.identifier]) {
                "Worker definition not found"
            }

            dao.update(worker.copy(state = LorraineInfo.State.RUNNING))

            val result = doWork(workLorraine = workerDefinition(), inputData = worker.inputData)
            val workerStatus = when (result) {
                is LorraineResult.Failure -> LorraineInfo.State.FAILED
                is LorraineResult.Retry -> LorraineInfo.State.ENQUEUED
                is LorraineResult.Success -> LorraineInfo.State.SUCCEEDED
            }

            dao.update(worker.copy(state = workerStatus))

            result
        }
            .fold(
                onSuccess = { result -> result.asWorkManagerResult() },
                onFailure = {
                    it.printStackTrace()
                    Result.failure()
                }
            )
    }

    private suspend fun doWork(
        workLorraine: WorkLorraine,
        inputData: LorraineData?
    ): LorraineResult {
        return workLorraine.doWork(inputData)
    }

    private fun LorraineResult.asWorkManagerResult() = when (this) {
        is LorraineResult.Failure -> Result.failure()
        is LorraineResult.Retry -> Result.retry()
        is LorraineResult.Success -> Result.success()
    }

}
