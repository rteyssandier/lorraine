package io.dot.lorraine.error

import kotlinx.coroutines.CancellationException

class LorraineWorkCancellation : CancellationException("Lorraine work has been cancelled")