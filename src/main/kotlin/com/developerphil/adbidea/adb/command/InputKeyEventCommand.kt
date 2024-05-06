package com.developerphil.adbidea.adb.command

import com.developerphil.adbidea.adb.command.receiver.GenericReceiver
import com.developerphil.adbidea.ui.NotificationHelper
import java.util.concurrent.TimeUnit

class InputKeyEventCommand(event: Int) : Command {

    companion object {
        const val BACK = 4
        const val UP = 19
        const val DOWN = 20
        const val LEFT = 21
        const val RIGHT = 22
        const val CENTER = 23
    }

    private val shellCommand = "input keyevent $event"

    override fun run(context: CommandContext): Boolean = with(context) {
        try {
            device.executeShellCommand(shellCommand, GenericReceiver(), 15L, TimeUnit.SECONDS)
            NotificationHelper.info(String.format("$shellCommand on %s", device.name))
            return true
        } catch (e1: Exception) {
            NotificationHelper.error(shellCommand + " failed... " + e1.message)
        }
        return false
    }
}