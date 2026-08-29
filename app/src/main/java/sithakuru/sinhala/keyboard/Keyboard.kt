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

@file:Suppress("ConvertLongToDuration")

package sithakuru.sinhala.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import sithakuru.sinhala.keyboard.data.Action
import sithakuru.sinhala.keyboard.datatype.KeyData

@SuppressLint("ViewConstructor")
class Keyboard(
    context: Context,
    prefs: Prefs,
    private val eventListener: EventListener,
    insetBottom: Int
) : View(context) {
    interface EventListener {
        fun onClickEvent(value: Int)
        fun swipeErase()
        fun swipeEraseUndo()
        fun swipeMoveRight()
        fun swipeMoveLeft()
    }

    private val colorText: Int = prefs.colorText
    private val textSizeFloat: Float = prefs.textSize.toFloat()

    private val paintBackground: Paint = Paint().apply {
        this.color = prefs.colorBackground
    }
    private val paintKeyNotPressed: Paint = Paint().apply {
        this.color = prefs.colorKeyBorder
        this.style = Paint.Style.STROKE
        this.strokeWidth = 0.5f.dpToPx()
    }
    private val paintKeyPressed: Paint = Paint().apply {
        this.color = prefs.colorPressedKeyBackground
        this.style = Paint.Style.FILL_AND_STROKE
        this.strokeWidth = 0.5f.dpToPx()
    }
    private val paintKeyLongPressed: Paint = Paint(paintKeyPressed).apply {
        this.color = prefs.colorLongPressedKeyBackground
    }
    private val paintKeyText: Paint = Paint().apply {
        this.color = colorText
        this.style = Paint.Style.FILL
        this.textSize = textSizeFloat.spToPx()
        this.textAlign = Paint.Align.CENTER
        this.isAntiAlias = true
    }
    private val paintKeyTextDim: Paint = Paint(paintKeyText).apply {
        this.color = prefs.colorTextDim
    }
    private val paintKeyTextSuperScript: Paint = Paint(paintKeyText).apply {
        this.color = prefs.colorTextLongPress
        this.textSize = (textSizeFloat / 2).spToPx()
    }

    private val keyBorders: Boolean = prefs.keyBorders

    private val longPressKeys: Boolean = prefs.longPressKeys
    private val longPressKeysLabels: Boolean = prefs.longPressKeysLabels

    private val drawableTheme = resources.newTheme()

    private val rowHeight = (48 * prefs.heightPercentage / 100).toFloat().dpToPx()

    private val paddingLeft: Float = 2f.dpToPx()
    private val paddingTop: Float = 2f.dpToPx()
    private val paddingRight: Float = 2f.dpToPx()
    private val paddingBottom: Float = 8f.dpToPx() + insetBottom
    private val borderPadding: Float = 1f.dpToPx()
    private val iconPadding: Float = 4f.dpToPx()

    /** Default width of a key. The final width of a key will be this multiplied by [KeyData.widthMultiplier] */
    private var keyWidth: Float = 48f.dpToPx()

    private val keyBorderRadius: Float = 4f.dpToPx()

    private val board: MutableMap<Int, MutableList<Key>> = mutableMapOf()
    private lateinit var pressedKeys: Map<Int, MutableSet<Int>>
    private lateinit var longPressedKeys: Map<Int, MutableSet<Int>>

    @DrawableRes
    private var currentActionButtonIcon: Int? = null

    private var actionDown: (motionEvent: MotionEvent) -> Boolean = { true }
    private var actionMove: (motionEvent: MotionEvent) -> Boolean = { true }
    private var actionUp: (motionEvent: MotionEvent) -> Boolean = { true }
    private var actionCancel: (motionEvent: MotionEvent) -> Boolean = { true }

    private val pointers: MutableMap<Int, PointerInfo> = mutableMapOf()

    private var swipeStepStartX: Float = 0F
    private val swipeStepDistance = resources.displayMetrics.widthPixels / 15
    private var swipeActionMode: Boolean = false

    @SwipeAction
    private var currentSwipeAction: Int = SwipeAction.NONE

    private var backspaceRowIndex: Int = NOT_FOUND_INDEX
    private var backspaceKeyIndex: Int = NOT_FOUND_INDEX
    private var backspaceDownTime = 0L
    private val backspaceRepeater = flow<Unit> {
        while (true) {
            val timeSinceLastDown = System.currentTimeMillis() - backspaceDownTime
            delay(
                when {
                    timeSinceLastDown > REPEAT_START_DELAY * 10 -> 8
                    timeSinceLastDown > REPEAT_START_DELAY * 8 -> 16
                    timeSinceLastDown > REPEAT_START_DELAY * 6 -> 32
                    timeSinceLastDown > REPEAT_START_DELAY * 4 -> 64
                    timeSinceLastDown > REPEAT_START_DELAY * 2 -> 128
                    else -> REPEAT_START_DELAY
                }
            )
            if (isBackspacePressed())
                performClick(Action.BACKSPACE)
            else
                break
        }
    }
    private val backspaceRepeaterStarter = flow<Unit> {
        delay(REPEAT_START_DELAY)
        backspaceRepeaterJob = backspaceRepeater.launchIn(CoroutineScope(Dispatchers.IO))
    }
    private var backspaceRepeaterJob: Job? = null
    private var backspaceRepeaterStarterJob: Job? = null

    private val longPressRedrawJobs: MutableList<Job> = mutableListOf()
    private fun getLongPressRedrawJob(pointerId: Int, rowIndex: Int, keyIndex: Int) = flow<Unit> {
        delay(POPUP_DELAY)
        if (pointers[pointerId]?.keyDownTime?.let { it != LONG_PRESS_NOT_AVAILABLE && getTime() - it > POPUP_DELAY } == true) {
            setLongPressedKey(rowIndex, keyIndex, true)
            invalidate()
        }
    }

    init {
        val swipeToEraseEnabled = prefs.swipeToErase
        val swipeToMoveCursorEnabled = prefs.swipeToMoveCursor
        if (swipeToEraseEnabled || swipeToMoveCursorEnabled) {
            val swipeActionSelector: (event: MotionEvent) -> Int = when {
                swipeToEraseEnabled && swipeToMoveCursorEnabled ->
                    { event: MotionEvent ->
                        when {
                            getRowIndexForY(event.getY(event.actionIndex)) > backspaceRowIndex -> SwipeAction.MOVE_CURSOR
                            else -> SwipeAction.ERASE
                        }
                    }

                swipeToEraseEnabled ->
                    { _: MotionEvent -> SwipeAction.ERASE }

                else ->
                    { _: MotionEvent -> SwipeAction.MOVE_CURSOR }
            }

            actionDown = { event: MotionEvent ->
                swipeActionMode = false
                currentSwipeAction =
                    if (event.pointerCount == 1) {
                        swipeStepStartX = event.x
                        swipeActionSelector(event)
                    } else {
                        SwipeAction.NONE
                    }
                event.onDown()
                true
            }
            actionMove = { event: MotionEvent ->
                if (currentSwipeAction != SwipeAction.NONE) {
                    val swipeDistance = swipeStepStartX - event.x
                    when {
                        swipeDistance > swipeStepDistance ->
                            when (currentSwipeAction) {
                                SwipeAction.ERASE -> {
                                    event.startSwipeMode()
                                    eventListener.swipeErase()
                                    swipeStepStartX = event.x
                                }

                                SwipeAction.MOVE_CURSOR -> {
                                    event.startSwipeMode()
                                    eventListener.swipeMoveLeft()
                                    swipeStepStartX = event.x
                                }
                            }

                        swipeDistance < -swipeStepDistance ->
                            when (currentSwipeAction) {
                                SwipeAction.ERASE -> {
                                    event.startSwipeMode()
                                    eventListener.swipeEraseUndo()
                                    swipeStepStartX = event.x
                                }

                                SwipeAction.MOVE_CURSOR -> {
                                    event.startSwipeMode()
                                    eventListener.swipeMoveRight()
                                    swipeStepStartX = event.x
                                }
                            }
                    }
                }
                if (!swipeActionMode) event.onMove()
                true
            }
            actionUp = { event: MotionEvent ->
                when (swipeActionMode) {
                    true -> swipeActionMode = false
                    false -> event.onUp()
                }
                true
            }
            actionCancel = { event: MotionEvent ->
                swipeActionMode = false
                event.onCancel()
                true
            }
        } else {
            actionDown = { event: MotionEvent ->
                event.onDown()
                true
            }
            actionMove = { event: MotionEvent ->
                event.onMove()
                true
            }
            actionUp = { event: MotionEvent ->
                event.onUp()
                true
            }
            actionCancel = { event: MotionEvent ->
                event.onCancel()
                true
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        keyWidth = (w - paddingLeft - paddingRight) / 10
        measureKeySizes()
    }

    private fun measureKeySizes() {
        for (row in board) {
            /** යතුරු පේළිය මැදට ගැනීම සඳහා (යතුරුවලින් පේළිය පිරී නැති අවස්ථාවලදී) */
            val rowLeft: Float = ((10 - row.value.sumOf { it.data.widthMultiplier.toDouble() }.toFloat()) / 2 * keyWidth)

            var previousKeyRight: Float = paddingLeft + rowLeft
            for (keyIndex in row.value.indices) {
                val keyTop = rowHeight * (row.key - 1) + paddingTop
                val keyRight = previousKeyRight + keyWidth * row.value[keyIndex].data.widthMultiplier
                val keyBottom = keyTop + rowHeight

                row.value[keyIndex].rect = RectF(previousKeyRight, keyTop, keyRight, keyBottom)

                row.value[keyIndex].data.inputData.iconRes?.let {
                    val iconDrawable = resources.getDrawable(it, drawableTheme)
                    iconDrawable.setTint(colorText)
                    iconDrawable.bounds = row.value[keyIndex].iconRect()
                    row.value[keyIndex].data.inputData.iconDrawable = iconDrawable
                }

                previousKeyRight = keyRight
            }
        }
        invalidate()
    }

    fun setActionKeyIcon(@DrawableRes iconRes: Int) {
        currentActionButtonIcon = iconRes
    }

    fun setRows(rows: Map<Int, List<KeyData>>) {
        for (row in rows) {
            val newRow: MutableList<Key> = mutableListOf()
            for (keyIndex in row.value.indices) {
                val keyData = row.value[keyIndex]

                if (row.key == 5 && keyIndex == 5)
                    keyData.inputData.iconRes = currentActionButtonIcon

                if (longPressKeys) {
                    if (!longPressKeysLabels)
                        keyData.popupInputData?.label = null
                } else keyData.popupInputData = null

                newRow.add(
                    Key(
                        index = keyIndex,
                        rect = RectF(),
                        data = keyData
                    )
                )
                if (keyData.inputData.value == Action.BACKSPACE) {
                    backspaceRowIndex = row.key
                    backspaceKeyIndex = keyIndex
                }
            }
            board[row.key] = newRow
        }
        pressedKeys = buildMap(rows.size) {
            for (row in rows) put(row.key, mutableSetOf())
        }
        longPressedKeys = buildMap(rows.size) {
            for (row in rows) put(row.key, mutableSetOf())
        }
        measureKeySizes()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val keyboardHeight = (rowHeight * 5 + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(resolveSize(ViewGroup.LayoutParams.MATCH_PARENT, widthMeasureSpec), keyboardHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            drawPaint(paintBackground)
            for (row in board)
                for (key in row.value) {
                    val isLongPressed = isKeyLongPressed(row.key, key.index)
                    if (isLongPressed)
                        drawRoundRect(key.borderRect(), keyBorderRadius, keyBorderRadius, paintKeyLongPressed)
                    else if (isKeyPressed(row.key, key.index))
                        drawRoundRect(key.borderRect(), keyBorderRadius, keyBorderRadius, paintKeyPressed)
                    else if (keyBorders)
                        drawRoundRect(key.borderRect(), keyBorderRadius, keyBorderRadius, paintKeyNotPressed)
                    key.data.inputData.label?.let {
                        drawText(
                            it, key.textCenterX(), key.textCenterY(),
                            if (key.data.inputData.dim) paintKeyTextDim else paintKeyText
                        )
                    }
                    key.data.popupInputData?.label?.let {
                        drawText(it, key.textSuperScriptCenterX(), key.textSuperScriptCenterY(), paintKeyTextSuperScript)
                    }
                    key.data.inputData.iconDrawable?.draw(canvas)
                }
        }
    }

    private fun isKeyPressed(rowIndex: Int, keyIndex: Int): Boolean = pressedKeys[rowIndex]?.contains(keyIndex) == true

    private fun isKeyLongPressed(rowIndex: Int, keyIndex: Int): Boolean = longPressedKeys[rowIndex]?.contains(keyIndex) == true

    private fun setPressedKey(rowIndex: Int, keyIndex: Int, pressed: Boolean) {
        if (pressed)
            pressedKeys[rowIndex]?.add(keyIndex)
        else
            pressedKeys[rowIndex]?.remove(keyIndex)

        invalidate()
    }

    private fun setLongPressedKey(rowIndex: Int, keyIndex: Int, pressed: Boolean) {
        if (pressed)
            longPressedKeys[rowIndex]?.add(keyIndex)
        else
            longPressedKeys[rowIndex]?.remove(keyIndex)

        invalidate()
    }

    private fun addLongPressRedrawJob(pointerId: Int, rowIndex: Int, keyIndex: Int) {
        longPressRedrawJobs.add(getLongPressRedrawJob(pointerId, rowIndex, keyIndex).launchIn(CoroutineScope(Dispatchers.IO)))
    }

    /**
     * Sets information about pointers.
     * @return `true`, if a pointer is set/updated. `false`, if nothing changed
     */
    private fun setPointerInfo(pointerId: Int, rowIndex: Int, keyIndex: Int, canLongPress: Boolean?): Boolean {
        var pointerChanged: Boolean
        val pointer = pointers[pointerId]
        if (keyIndex == NOT_FOUND_INDEX)
            pointerChanged = removePointerInfo(pointerId, pointer)
        else {
            if (pointer == null) {
                pointers[pointerId] = PointerInfo(rowIndex, keyIndex, if (canLongPress == true) getTime() else LONG_PRESS_NOT_AVAILABLE)

                if (canLongPress == true)
                    addLongPressRedrawJob(pointerId, rowIndex, keyIndex)

                pointerChanged = true
            } else if (pointer.rowIndex != rowIndex || pointer.keyIndex != keyIndex) {
                setPressedKey(pointer.rowIndex, pointer.keyIndex, false)
                setLongPressedKey(pointer.rowIndex, pointer.keyIndex, false)
                pointers[pointerId]?.rowIndex = rowIndex
                pointers[pointerId]?.keyIndex = keyIndex
                pointers[pointerId]?.keyDownTime = LONG_PRESS_NOT_AVAILABLE

                pointerChanged = true
            } else {
                pointerChanged = false
            }
        }
        if (pointerChanged) setPressedKey(rowIndex, keyIndex, true)
        return pointerChanged
    }

    private fun removePointerInfo(pointerId: Int, pointer: PointerInfo? = pointers[pointerId]): Boolean {
        pointer?.let {
            setPressedKey(it.rowIndex, it.keyIndex, false)
            setLongPressedKey(it.rowIndex, it.keyIndex, false)
        }
        return pointers.remove(pointerId) != null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean = when (event?.actionMasked) {
        MotionEvent.ACTION_DOWN,
        MotionEvent.ACTION_POINTER_DOWN ->
            actionDown(event)

        MotionEvent.ACTION_MOVE ->
            actionMove(event)

        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_POINTER_UP ->
            actionUp(event)

        MotionEvent.ACTION_CANCEL ->
            actionCancel(event)

        else -> false
    }

    private fun MotionEvent.startSwipeMode() {
        if (!swipeActionMode) {
            swipeActionMode = true
            removePointerInfo(getPointerId())
        }
    }

    private fun getKeyIndexForRowX(rowIndex: Int, x: Float): Int = board[rowIndex]?.let { row ->
        for (key in row)
            if (x in key.rect.left..key.rect.right)
                return@let key.index
        null
    } ?: NOT_FOUND_INDEX

    private fun getRowIndexForY(pointerY: Float): Int = when {
        pointerY < paddingTop -> NOT_FOUND_INDEX
        pointerY > rowHeight * board.size + paddingTop -> NOT_FOUND_INDEX
        else -> ((pointerY - paddingTop) / rowHeight).mod(board.size.toFloat()).toInt() + 1
    }

    private fun MotionEvent.onDown() {
        rowKeyAndValues { rowIndex, keyIndex, value, popupValue ->
            setPointerInfo(getPointerId(), rowIndex, keyIndex, popupValue != null)
            if (value == Action.BACKSPACE) {
                backspaceDownTime = getTime()
                backspaceRepeaterStarterJob = backspaceRepeaterStarter.launchIn(CoroutineScope(Dispatchers.IO))
            }
        }
    }

    private fun MotionEvent.onMove() {
        for (pointerIndex in 0..<pointerCount) {
            val rowIndex: Int = getRowIndexForY(getY(pointerIndex))
            val keyIndex: Int = getKeyIndexForRowX(rowIndex, getX(pointerIndex))
            setPointerInfo(getPointerId(pointerIndex), rowIndex, keyIndex, null)
        }
    }

    private fun MotionEvent.onUp() {
        rowKeyAndValues { rowIndex, keyIndex, value, popupValue ->
            if (value != null) when (value) {
                Action.BACKSPACE -> {
                    backspaceRepeaterStarterJob?.cancel()
                    backspaceRepeaterJob?.cancel()
                    if (getTime() - backspaceDownTime < REPEAT_START_DELAY)
                        performClick(Action.BACKSPACE)
                }

                else -> {
                    if (popupValue != null && isKeyLongPressed(rowIndex, keyIndex))
                        performClick(popupValue)
                    else
                        performClick(value)
                }
            }
        }
        removePointerInfo(getPointerId())
        invalidate()
    }

    private fun MotionEvent.onCancel() {
        removePointerInfo(getPointerId())
        invalidate()
    }

    private fun MotionEvent.rowKeyAndValues(action: (rowIndex: Int, keyIndex: Int, keyData: Int?, popupValue: Int?) -> Unit) {
        val rowIndex: Int = getRowIndexForY(getY(actionIndex))
        val keyIndex: Int = getKeyIndexForRowX(rowIndex, this.getX(this.actionIndex))
        action(
            rowIndex,
            keyIndex,
            board[rowIndex]?.getOrNull(keyIndex)?.data?.inputData?.value,
            board[rowIndex]?.getOrNull(keyIndex)?.data?.popupInputData?.value,
        )
    }

    private fun MotionEvent.getPointerId(): Int = this.getPointerId(this.actionIndex)

    private fun performClick(value: Int) {
        eventListener.onClickEvent(value)
        performClick()
    }

    private fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
    private fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)

    private fun getTime(): Long = System.currentTimeMillis()

    private fun isBackspacePressed(): Boolean {
        for (pointer in pointers.values) if (pointer.rowIndex == backspaceRowIndex && pointer.keyIndex == backspaceKeyIndex) return true
        return false
    }

    private data class Key(
        val index: Int,
        var rect: RectF,
        var data: KeyData
    )

    private data class PointerInfo(
        var rowIndex: Int,
        var keyIndex: Int,
        var keyDownTime: Long
    )

    private fun Key.borderRect(): RectF = RectF(this.rect).apply { this.inset(borderPadding, borderPadding) }
    private fun Key.textCenterX(): Float = this.borderRect().centerX()
    private fun Key.textCenterY(): Float = this.borderRect().centerY() + rowHeight / 6
    private fun Key.textSuperScriptCenterX(): Float = this.borderRect().centerX() + keyWidth * 0.25f
    private fun Key.textSuperScriptCenterY(): Float = this.borderRect().centerY() - rowHeight * 0.25f
    private fun Key.iconRect(): Rect {
        val borderRect = this.borderRect()
        val iconSize: Int = borderRect.height().coerceAtMost(keyWidth - borderPadding).toInt()
        val rectLeft: Int = (borderRect.centerX() - iconSize / 2).toInt()
        val rectTop: Int = (borderRect.centerY() - iconSize / 2).toInt()

        return Rect(rectLeft, rectTop, rectLeft + iconSize, rectTop + iconSize).apply {
            inset((iconPadding * 2).toInt(), (iconPadding * 2).toInt())
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SwipeAction.ERASE, SwipeAction.MOVE_CURSOR, SwipeAction.NONE)
    private annotation class SwipeAction {
        companion object {
            const val ERASE: Int = 1
            const val MOVE_CURSOR: Int = 2
            const val NONE: Int = 3
        }
    }

    companion object {
        private const val NOT_FOUND_INDEX: Int = -1
        private const val LONG_PRESS_NOT_AVAILABLE: Long = -1
        private const val REPEAT_START_DELAY: Long = 300
        private const val POPUP_DELAY: Long = 300
    }
}
