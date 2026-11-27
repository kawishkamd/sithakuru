/*
 * This file is part of Sithakuru.
 *
 * Sithakuru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Sithakuru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sithakuru.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package sithakuru.sinhala.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sithakuru.sinhala.keyboard.data.EmojiCategoryId
import sithakuru.sinhala.keyboard.data.EmojiData

class EmojiDataTest {

    @Test
    fun filterCategoriesKeepsAllWhenPredicateAlwaysTrue() {
        val result = EmojiData.filterCategories { true }
        assertEquals(EmojiData.categories, result)
    }

    @Test
    fun filterCategoriesDropsUnsupportedGlyphsAndEmptyCategories() {
        val allowed = setOf("😀", "🇱🇰")
        val result = EmojiData.filterCategories { it in allowed }

        assertEquals(2, result.size)
        assertEquals(EmojiCategoryId.SMILEYS, result[0].id)
        assertEquals(listOf("😀"), result[0].emojis)
        assertEquals(EmojiCategoryId.FLAGS, result[1].id)
        assertEquals(listOf("🇱🇰"), result[1].emojis)
    }

    @Test
    fun filterCategoriesReturnsEmptyListWhenNoneSupported() {
        assertTrue(EmojiData.filterCategories { false }.isEmpty())
    }
}
