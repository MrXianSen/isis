package org.apache.isis.client.kroviz

import org.apache.isis.client.kroviz.ui.IconManager
import kotlin.test.Test
import kotlin.test.assertEquals

class IconManagerTest {

    @Test
    fun testFind() {
        //given
        val name = "OK"
        val expected = "fas fa-check"
        //when
        val actual = IconManager.find(name)
        //
        assertEquals(expected, actual)
    }

}
