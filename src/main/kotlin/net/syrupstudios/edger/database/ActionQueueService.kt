package net.syrupstudios.edger.database

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.syrupstudios.edger.Ledger
import net.syrupstudios.edger.actions.ActionType
import net.syrupstudios.edger.config.DatabaseSpec
import net.syrupstudios.edger.logWarn
import net.syrupstudios.edger.utility.ticks
import java.util.concurrent.LinkedBlockingQueue

object ActionQueueService {
    private val queue = LinkedBlockingQueue<ActionType>()
    private val wake = Channel<Unit>(Channel.CONFLATED)
    private val stateLock = Any()
    @Volatile private var stopping = false
    private var job: Job? = null

    val size: Int get() = queue.size

    fun start() = synchronized(stateLock) {
        if (stopping || job != null) return@synchronized
        job = Ledger.launch { runWorker() }
    }

    fun addToQueue(action: ActionType): Boolean {
        if (action.isBlacklisted()) return false

        return queue.add(action)
    }

    suspend fun drainAll() {
        val worker = synchronized(stateLock) {
            stopping = true
            job
        }
        wake.trySend(Unit)
        worker?.join()
        drainPending()
    }

    private suspend fun runWorker() {
        while (!stopping) {
            if (queue.size < Ledger.config[DatabaseSpec.batchSize]) {
                withTimeoutOrNull(Ledger.config[DatabaseSpec.batchDelay].ticks) { wake.receive() }
            }
            if (stopping) break
            writeBatch()
        }
    }

    private suspend fun drainPending() {
        while (queue.isNotEmpty()) {
            writeBatch()
        }
    }

    private suspend fun writeBatch() {
        try {
            val batch = queue.take(Ledger.config[DatabaseSpec.batchSize])
            if (batch.isEmpty()) return
            DatabaseManager.logActionBatch(batch)
            repeat(batch.size) { queue.poll() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logWarn("Could not write action batch. Retrying.", e)
            delay(1000)
        }
    }
}
