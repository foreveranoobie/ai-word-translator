package com.storozhuk.translator.ui

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.setPrimary
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.service.WordsService
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route

@Route("")
class MainPage(private val wordsService: WordsService) : KComposite() {

    private lateinit var words: MutableMap<String, WordExplanationData>
    private lateinit var grid: Grid<WordRowData>

    private val root = ui {
        verticalLayout {

            val horizonalLayout = addExplainInput()

            this.add(horizonalLayout)
            words = wordsService.getAllWordsWithContent()
            initWordsTable()
            updateWordsGridItems()
            this.add(grid)
        }
    }

    private fun showDetailDialog(wordExplanation: WordExplanationData) {
        val dialog = Dialog()
        dialog.maxWidth = "80%"
        dialog.maxHeight = "80%"
        dialog.headerTitle = wordExplanation.word

        val content = VerticalLayout()
        val definitions = UnorderedList()
        for (definition in wordExplanation.definitions!!) {
            val meaning = UnorderedList()
            val meaningAndUsageItems = ListItem(Div(definition.meaning), Div(definition.usage))
            meaning.add(meaningAndUsageItems)
            val example = UnorderedList()
            example.add(ListItem("Example: ${definition.example!!.foreignLanguage}"))
            example.add(ListItem("Original: ${definition.example.english}"))
            meaning.add(example)
            meaning.style.setMarginBottom("15px")
            definitions.add(meaning)
        }
        content.add(definitions)

        val closeButton = Button("Close") {
            dialog.close()
        }

        dialog.add(content, closeButton)
        dialog.open()
    }

    private fun initWordsTable() {
        grid = Grid<WordRowData>()
        grid.addColumn { it.word }.setHeader("Word")
        grid.addColumn { it.translation }.setHeader("Translation")

        grid.addSelectionListener { event ->
            event.firstSelectedItem.ifPresent { selectedWord ->
                showDetailDialog(words[selectedWord.word]!!)
            }
        }
    }

    private fun addExplainInput(): HorizontalLayout {
        val horizonalLayout = HorizontalLayout()
        val fieldName = TextField("Enter the word to explain")
        fieldName.className = "bordered"
        horizonalLayout.add(fieldName)
        val button = Button()
        button.text = "Explain"
        button.setPrimary()
        button.addClickShortcut(Key.ENTER)
        button.onLeftClick {
            if (!fieldName.value.trim().isEmpty()) {
                val explanation = wordsService.explainWord(fieldName.value.toString())
                showDetailDialog(explanation)
                words.put(explanation.word!!, explanation)
                updateWordsGridItems()
                fieldName.value = ""
            }
        }
        button.height = "50%"
        horizonalLayout.add(button)
        horizonalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, button)
        return horizonalLayout
    }

    private fun updateWordsGridItems() {
        grid.setItems(
            wordsService.getTranslationsFromExplanationDataList(words.values)
        )
    }
}