package com.androdevlinux.percy.bluetoothprinterdemo

/**
 * Created by percy on 06/07/2018.
 */

object ESCPOSCommands {
    internal val LF = byteArrayOf(0x0A)
    private const val ESC: Byte = 0x1B
    private const val FS: Byte = 0x1C
    internal val FS_FONT_SIZE_BOLD = byteArrayOf(FS, 0x21, 35, ESC, 0x21, 24)
    internal val FS_FONT_SIZE_NON_BOLD = byteArrayOf(FS, 0x21, 32, ESC, 0x21, 6)
}
