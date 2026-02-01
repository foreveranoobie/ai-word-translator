package com.storozhuk.translator.ui

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.alignSelf
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalAlignSelf
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.setPrimary
import com.github.mvysny.kaributools.textAlign
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.service.WordsService
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route

@Route("")
class MainPage(private val wordsService: WordsService) : KComposite() {
    private val root = ui {
        verticalLayout {

            val horizonalLayout = addExplainInput()

            this.add(horizonalLayout)
            this.add(initWordsTable())
        }
    }

    private fun showDetailDialog(wordRowData: WordRowData) {
        val dialog = Dialog()
        dialog.maxWidth = "80%"
        dialog.maxHeight = "80%"
        dialog.headerTitle = wordRowData.word

        val content = VerticalLayout()
        content.add(wordRowData.explanation)

        val closeButton = Button("Close") {
            dialog.close()
        }

        dialog.add(content, closeButton)
        dialog.open()
    }

    private fun initWordsTable(): Grid<WordRowData>{
        val grid = Grid<WordRowData>()
        grid.addColumn { it.word }.setHeader("Word")
        grid.addColumn { it.explanation }.setHeader("Translation")

        // Sample data
        grid.setItems(
            wordsService.getAllWordsWithContent()
        )

        grid.addSelectionListener { event ->
            event.firstSelectedItem.ifPresent { selectedWord ->
                showDetailDialog(selectedWord)
            }
        }
        return grid
    }

    private fun addExplainInput(): HorizontalLayout{
        val horizonalLayout = HorizontalLayout()
        val fieldName = TextField("Enter the word to explain")
        fieldName.className = "bordered"
        horizonalLayout.add(fieldName)
        val button = Button()
        button.text = "Explain"
        button.setPrimary()
        button.addClickShortcut(Key.ENTER)
        button.onLeftClick {
            wordsService.explainWord(fieldName.value.toString())
        }
        button.height = "50%"
        horizonalLayout.add(button)
        return horizonalLayout
    }
}