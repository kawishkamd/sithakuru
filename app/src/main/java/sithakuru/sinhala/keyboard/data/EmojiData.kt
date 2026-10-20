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

package sithakuru.sinhala.keyboard.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import sithakuru.sinhala.keyboard.R

enum class EmojiCategoryId(@StringRes val labelRes: Int) {
    RECENT(R.string.emoji_category_recent),
    SMILEYS(R.string.emoji_category_smileys),
    PEOPLE(R.string.emoji_category_people),
    ANIMALS_NATURE(R.string.emoji_category_animals_nature),
    FOOD_DRINK(R.string.emoji_category_food_drink),
    ACTIVITIES(R.string.emoji_category_activities),
    TRAVEL(R.string.emoji_category_travel),
    OBJECTS_SYMBOLS(R.string.emoji_category_objects_symbols),
    FLAGS(R.string.emoji_category_flags)
}

data class EmojiCategory(
    val id: EmojiCategoryId,
    @DrawableRes val iconRes: Int,
    val emojis: List<String>
) {
    val isRecent: Boolean
        get() = id == EmojiCategoryId.RECENT
}

object EmojiData {
    val categories: List<EmojiCategory> = listOf(
        EmojiCategory(
            id = EmojiCategoryId.SMILEYS,
            iconRes = R.drawable.ic_category_smileys,
            emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
                "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
                "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
                "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
                "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨",
                "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨",
                "😰", "😥", "😓", "🫣", "🤗", "🫡", "🤔", "🫢", "🤫", "🤥",
                "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲",
                "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧",
                "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡",
                "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.PEOPLE,
            iconRes = R.drawable.ic_category_people,
            emojis = listOf(
                "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘",
                "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵", "👋", "🤚",
                "🖐️", "✋", "🖖", "🫱", "🫲", "👏", "🙌", "👐", "🤲", "🤝",
                "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
                "🦻", "👃", "🫀", "🫁", "🧠", "👀", "👁️", "👅", "👄", "💋",
                "👶", "👧", "🧒", "👦", "👩", "🧑", "👨", "👵", "🧓", "👴",
                "👲", "👳", "🧕", "👮", "👷", "💂", "🕵️", "🧑‍⚕️", "🧑‍🎓", "🧑‍🏫",
                "🧑‍🍳", "🧑‍🌾", "🧑‍💻", "🧑‍🔬", "🧑‍🎨", "🧑‍🚒", "🧑‍✈️", "🧑‍🚀", "🧑‍⚖️", "🦸",
                "🦹", "🧙", "🧚", "🧛", "🧜", "🧝", "🧞", "🧟", "🚶", "🧍",
                "🧎", "🏃", "💃", "🕺", "👫", "👭", "👬", "🧑‍🤝‍🧑", "🫂"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.ANIMALS_NATURE,
            iconRes = R.drawable.ic_category_animals,
            emojis = listOf(
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
                "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒",
                "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇",
                "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌", "🐞",
                "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️", "🕸️", "🦂", "🐢",
                "🐍", "🦎", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟",
                "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧",
                "🐘", "🦛", "🦏", "🐪", "🐫", "🦒", "🦘", "🦬", "🐃", "🐂",
                "🐄", "🐎", "🐖", "🐏", "🐑", "🦙", "🐐", "🦌", "🐕", "🐩",
                "🦮", "🐕‍🦺", "🐈", "🐈‍⬛", "🐓", "🦃", "🦚", "🦜", "🦢", "🦩",
                "🕊️", "🐇", "🦝", "🦨", "🦡", "🦫", "🦦", "🦥", "🐁", "🐀",
                "🐿️", "🦔", "🐾", "🌲", "🌳", "🌴", "🌵", "🌷", "🌸", "🌹",
                "🌺", "🌻", "🌼", "💐", "🌱", "🌿", "☘️", "🍀", "🍁", "🍂", "🍃"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.FOOD_DRINK,
            iconRes = R.drawable.ic_category_food,
            emojis = listOf(
                "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
                "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🍆",
                "🥔", "🥕", "🌽", "🌶️", "🫑", "🥒", "🥬", "🥦", "🧄", "🧅",
                "🥜", "🫘", "🌰", "🍞", "🥐", "🥖", "🫓", "🥨", "🥯", "🥞",
                "🧇", "🧀", "🍖", "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭",
                "🥪", "🌮", "🌯", "🫔", "🥙", "🧆", "🥚", "🍳", "🥘", "🍲",
                "🫕", "🥣", "🥗", "🍿", "🧈", "🧂", "🥫", "🍱", "🍘", "🍙",
                "🍚", "🍛", "🍜", "🍝", "🍠", "🍢", "🍣", "🍤", "🍥", "🥮",
                "🍡", "🥟", "🥠", "🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰",
                "🧁", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯", "🍼", "🥛", "☕",
                "🫖", "🍵", "🍶", "🍾", "🍷", "🍸", "🍹", "🍺", "🍻", "🥂",
                "🥃", "🥤", "🧋", "🧃", "🧉", "🧊"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.ACTIVITIES,
            iconRes = R.drawable.ic_category_activities,
            emojis = listOf(
                "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
                "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
                "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷",
                "⛸️", "🥌", "🎿", "⛷️", "🏂", "🏋️", "🤼", "🤸", "🤺", "🏇",
                "🎯", "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️", "🎫", "🎟️", "🎪",
                "🎭", "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺",
                "🎸", "🪕", "🎻", "🎲", "♟️", "🎮", "🕹️", "🎰"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.TRAVEL,
            iconRes = R.drawable.ic_category_travel,
            emojis = listOf(
                "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
                "🛻", "🚚", "🚛", "🚜", "🛵", "🏍️", "🛺", "🚲", "🛴", "🚨",
                "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", "🚃", "🚋", "🚞",
                "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇", "🚊", "🚉", "🚁",
                "✈️", "🛫", "🛬", "🚀", "🛸", "🛰️", "⛵", "🚤", "🛥️", "🚢",
                "⚓", "⛽", "🚧", "🚦", "🚥", "🗿", "🗽", "🗼", "🏰", "🏯",
                "🏟️", "🎡", "🎢", "⛲", "⛱️", "🏖️", "🏝️", "🏜️", "🌋", "⛰️",
                "🏔️", "🏕️", "⛺", "🏠", "🏡", "🏢", "🏣", "🏤", "🏥", "🏦",
                "🏨", "🏪", "🏫", "🏬", "🏭", "🏛️", "⛪", "🕌", "🛕", "🕍"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.OBJECTS_SYMBOLS,
            iconRes = R.drawable.ic_category_objects,
            emojis = listOf(
                "💡", "🔦", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "📷",
                "📸", "📹", "🎥", "📽️", "📞", "☎️", "📺", "📻", "🎙️", "⏱️",
                "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋", "🔌", "🕯️", "💸",
                "💵", "💴", "💶", "💷", "🪙", "💰", "💳", "💎", "⚖️", "🧰",
                "🔧", "🔨", "⚒️", "🛠️", "⛏️", "🔩", "⚙️", "🧱", "⛓️", "🧲",
                "🔫", "💣", "🧨", "🔪", "🗡️", "⚔️", "🛡️", "🔑", "🗝️", "🔒",
                "🔓", "🔏", "🔐", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤",
                "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗",
                "💖", "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉️", "☸️", "✡️",
                "🔯", "🕎", "☯️", "☦️", "🛐", "🆔", "⚛️", "🛑", "⛔", "🚫",
                "💯", "💢", "♨️", "❗", "❕", "❓", "❔", "‼️", "⁉️", "⚠️",
                "✅", "❌", "❎", "❇️", "✳️", "🌐", "Ⓜ️", "🌀", "💤", "🏧",
                "♿", "🅿️", "🚹", "🚺", "🚼", "🚻", "📶", "🆗", "🆒", "🆕",
                "🆓", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣",
                "9️⃣", "🔟", "▶️", "⏸️", "⏹️", "⏺️", "⏭️", "⏮️", "⏩", "⏪",
                "🔀", "🔁", "🔂", "➡️", "⬅️", "⬆️", "⬇️", "🔄", "ℹ️", "🎵",
                "🎶", "➕", "➖", "➗", "✖️", "♾️", "💲", "💱", "™️", "©️",
                "®️", "♠️", "♣️", "♥️", "♦️", "🔔", "🔕", "📣", "📢", "💬",
                "💭", "🗯️", "🎉", "✨", "🔥", "⭐", "🌟"
            )
        ),
        EmojiCategory(
            id = EmojiCategoryId.FLAGS,
            iconRes = R.drawable.ic_category_flags,
            emojis = listOf(
                "🇱🇰", "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
                "🇦🇺", "🇧🇷", "🇨🇦", "🇨🇳", "🇫🇷", "🇩🇪", "🇮🇳", "🇮🇹", "🇯🇵", "🇲🇻",
                "🇳🇵", "🇳🇿", "🇵🇰", "🇷🇺", "🇸🇬", "🇿🇦", "🇰🇷", "🇪🇸", "🇬🇧", "🇺🇸"
            )
        )
    )
}
