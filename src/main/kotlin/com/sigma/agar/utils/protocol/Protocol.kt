package com.sigma.agar.utils.protocol

import com.sigma.agar.physics.things.Thing
import com.sigma.agar.utils.Tuple
import org.webutil.httpserver.Session

interface Protocol {
    fun dispatch(center: Tuple, width: Float, height: Float, things: List<Thing>, end_time_ms: Long, session: Session)
}