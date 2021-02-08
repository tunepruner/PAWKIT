package com.tunepruner.bomboleguerodemo.trigger.triggergraph.triggerzone

import android.graphics.PointF
import com.tunepruner.bomboleguerodemo.trigger.triggergraph.triggerzone.zonelayer.LayerZone
import com.tunepruner.bomboleguerodemo.trigger.triggergraph.triggerzone.zonelayer.ZoneLimits

interface TriggerZone {
    fun isMatch(pointF: PointF): Boolean
    fun invokeLayer(pointF: PointF): LayerZone?
    fun addLayer(triggerLayerZone: LayerZone)
    fun getLayer(zoneLayer: Int): LayerZone
    fun getLimits(): ZoneLimits
}