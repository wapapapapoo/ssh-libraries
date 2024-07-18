package one.iolab.utils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import one.iolab.app.iostreamwrapper.ByteConsumer
import one.iolab.app.iostreamwrapper.ByteProvider

public class CliUtils {

    companion object {

        /**
         * 计算字符的视觉宽度
         *
         * 不精确，仅适用中英混排
         */
        @JvmStatic
        public fun charcterVisualWidth(char: String): Int {
            if (char.length == 0) {
                return 0
            }
            if (char[0].code >= 32 && char[0].code <= 126) {
                return 1
            } else if (char[0].code >= 128) {
                return 2
            }
            return 0
        }

        @JvmStatic
        public fun utf8InputBuffer(
                input: ByteProvider,
                output: ByteConsumer,
                line_start: String = "$ ",
                line_start_width: Int = 2,
                data: ArrayList<String> = ArrayList(),
                interrupt: () -> Boolean
        ): String {

            val charset: Charset = StandardCharsets.UTF_8
            output.accept(("\r" + line_start).toByteArray())

            var char_buffer: ArrayList<String> = data
            var logic_cursor = char_buffer.size
            var visual_cursor = line_start_width

            val put_char: (String) -> Unit = { ch ->
                // val width: Int = charcterVisualWidth(ch)
                output.accept(ch.toByteArray(charset))
            }

            val move_visual_cursor_tpl: String =
                    StringBuilder().append("\r").append(27.toChar()).append("[%dC").toString()

            val move_visual_cursor: (Int) -> Unit = { visual_offset ->
                output.accept(
                        String.format(move_visual_cursor_tpl, visual_offset).toByteArray(charset)
                )
            }

            val move_cursor: (Int) -> Unit = { logic_offset ->
                var width: Int = line_start_width
                if (logic_offset != 0) {
                    for (i in 0..(logic_offset - 1)) {
                        width += charcterVisualWidth(char_buffer[i])
                    }
                }
                move_visual_cursor(width)
                logic_cursor = logic_offset
                visual_cursor = width
            }

            val put_ln: (Int) -> Unit = { start ->
                val sb: StringBuilder = StringBuilder()
                sb.append(27.toChar())
                sb.append("[0K")
                output.accept(sb.toString().toByteArray(charset))

                var visual_offset: Int = visual_cursor
                if (start < char_buffer.size) {
                    for (i in start..char_buffer.size - 1) {
                        put_char(char_buffer[i])
                        visual_offset += charcterVisualWidth(char_buffer[i])
                        move_visual_cursor(visual_offset)
                    }
                }
                move_visual_cursor(visual_cursor)
            }

            put_ln(0)
            char_buffer.forEach({ char -> visual_cursor += charcterVisualWidth(char) })
            move_visual_cursor(visual_cursor)

            while (!interrupt()) {
                var chba: ByteArray? = input.getByte()
                if (chba == null || chba.size == 0) {
                    val sb: StringBuilder = StringBuilder()
                    for (c in char_buffer) {
                        sb.append(c)
                    }
                    return sb.toString()
                }

                var ch: Int = chba[0].toInt()

                if (ch == '\n'.code || ch == '\r'.code) {
                    val sb: StringBuilder = StringBuilder()
                    for (c in char_buffer) {
                        sb.append(c)
                    }
                    return sb.toString()
                } else if (ch == '\b'.code || ch == 127) {
                    if (logic_cursor > 0) {
                        val cur_char = char_buffer.removeAt(logic_cursor - 1)
                        logic_cursor--
                        visual_cursor -= charcterVisualWidth(cur_char)
                        move_visual_cursor(visual_cursor)
                        put_ln(logic_cursor)
                    }
                } else if (ch == '\t'.code) {
                    logic_cursor = 0
                    visual_cursor = line_start_width
                    move_visual_cursor(visual_cursor)
                    char_buffer.clear()
                } else if (ch == 3) {
                    return "exit"
                } else if (ch == 27) {
                    chba = input.getByte()
                    if (chba == null || chba.size == 0) {
                        val sb: StringBuilder = StringBuilder()
                        for (c in char_buffer) {
                            sb.append(c)
                        }
                        return sb.toString()
                    }
                    ch = chba[0].toInt()

                    // 方向键
                    if (ch == 91) {
                        chba = input.getByte()
                        if (chba == null || chba.size == 0) {
                            val sb: StringBuilder = StringBuilder()
                            for (c in char_buffer) {
                                sb.append(c)
                            }
                            return sb.toString()
                        }
                        ch = chba[0].toInt()

                        when (ch) {
                            65 -> {
                                return "prev"
                            }
                            66 -> {
                                return "next"
                            }
                            67 -> {
                                if (logic_cursor < char_buffer.size) {
                                    move_cursor(logic_cursor + 1)
                                }
                            }
                            68 -> {
                                if (logic_cursor > 0) {
                                    move_cursor(logic_cursor - 1)
                                }
                            }
                        }
                    }
                } else if (ch >= 32 && ch <= 126) {
                    char_buffer.add(logic_cursor, ch.toChar().toString())
                    put_ln(logic_cursor)
                    move_cursor(logic_cursor + 1)
                } else if (ch < 0) {
                    // utf-8扩展区
                    val wcharbuf: ArrayList<Int> = ArrayList()
                    wcharbuf.add(ch)
                    val first_byte: Int = ch

                    // 0b11000000 0b10000000
                    if (first_byte >= -64) {
                        ch = input.getByte()[0].toInt()
                        wcharbuf.add(ch)
                    }
                    // 0b11100000 0b10000000 0b10000000
                    if (first_byte >= -32) {
                        ch = input.getByte()[0].toInt()
                        wcharbuf.add(ch)
                    }
                    // 0b11110000 0b10000000 0b10000000 0b10000000
                    if (first_byte >= -16) {
                        ch = input.getByte()[0].toInt()
                        wcharbuf.add(ch)
                    }

                    var str: ByteArray = ByteArray(wcharbuf.size)
                    var i = 0
                    for (c in wcharbuf) {
                        str.set(i++, c.toByte())
                    }

                    char_buffer.add(logic_cursor, str.toString(StandardCharsets.UTF_8))
                    put_ln(logic_cursor)
                    move_cursor(logic_cursor + 1)
                }
            }

            return "exit"
        }
    }
}
