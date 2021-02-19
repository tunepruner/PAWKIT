package com.tunepruner.fingerperc.instrument

import android.util.Log
import android.view.MotionEvent
import com.tunepruner.fingerperc.graphics.GUIManager
import com.tunepruner.fingerperc.sample.SampleManager
import com.tunepruner.fingerperc.sample.samplelibrary.samplegroup.samplelayer.playable.Playable
import com.tunepruner.fingerperc.trigger.TriggerManager
import java.io.IOException

class SimplePlayer(
    private val touchLogic: TouchLogic,
    private val triggerManager: TriggerManager,
    private val sampleManager: SampleManager,
    private val GUIManager: GUIManager,
    private val resourceManager: ResourceManager

) : Player {
    private val jniPlayerAdapter = JNIPlayerAdapter()

    init {
        prepare()
        jniPlayerAdapter.loadAllAssets(resourceManager)
    }

    override fun play(event: MotionEvent) {
        val pointF = touchLogic.reduceTouchEvent(event)
        if (pointF != null) {
            val zoneLayer = triggerManager.computeZoneLayer(pointF)
            Log.i("zoneLayer", zoneLayer.getLayerIteration().toString())
            val playable = sampleManager.computeSample(zoneLayer)
            jniPlayerAdapter.play(playable)
            GUIManager.startAnimation(zoneLayer)
        }
    }

    override fun tearDown() {
        jniPlayerAdapter.unloadWavAssetsNative()
        jniPlayerAdapter.teardownAudioStreamNative()
    }

    fun prepare(){
        jniPlayerAdapter.setupAudioStreamNative(1)
        jniPlayerAdapter.startAudioStreamNative()
        jniPlayerAdapter.loadAllAssets(resourceManager)
    }
}

class JNIPlayerAdapter{
    val TAG = "JNIPlayerAdapter"
    fun play(playable: Playable){
        trigger(playable.getIndex())
    }


    fun loadAllAssets(resourceManager: ResourceManager): Boolean{
        var allAssetsCorrect = true

        for (element in resourceManager.fileSnapshots) {
            allAssetsCorrect = loadWavAssetLocal(element) && allAssetsCorrect
        }

        return allAssetsCorrect
    }

    external fun setupAudioStreamNative(numChannels: Int)

    external fun startAudioStreamNative()

    external fun teardownAudioStreamNative()

    private fun loadWavAssetLocal(fileSnapshot: FileSnapshot): Boolean{
        var returnVal = false
        try {
//            val assetFD = fileSnapshot.assetFileDescriptor
//            val dataStream = assetFD.createInputStream()
//            val dataLen = assetFD.getLength().toInt()
//            val dataBytes = ByteArray(dataLen)
//            dataStream.read(dataBytes, 0, dataLen)
//            returnVal = loadWavAssetNative(dataBytes, fileSnapshot.index, .5F, 1)
//            assetFD.close()
            val assetMgr = fileSnapshot.assetManager
            val assetFD = assetMgr.openFd(fileSnapshot.fileName)
            val dataStream = assetFD.createInputStream()
            val dataLen = assetFD.getLength().toInt()
            val dataBytes = ByteArray(dataLen)
            dataStream.read(dataBytes, 0, dataLen)
            returnVal = loadWavAssetNative(dataBytes, 0, 0.5F, 1)
            assetFD.close()
        } catch (ex: IOException) {
            Log.i(TAG, "IOException$ex")
        }
        return returnVal
    }

    external fun loadWavAssetNative(
        wavBytes: ByteArray, index: Int, pan: Float, channels: Int) : Boolean

    external fun unloadWavAssetsNative()

    external fun trigger(drumIndex: Int)

//    external fun getOutputReset() : Boolean
//
//    external fun clearOutputReset()
//
//    external fun restartStream()
}