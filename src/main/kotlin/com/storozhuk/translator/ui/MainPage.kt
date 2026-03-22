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
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route

@Route("")
class MainPage(private val wordsService: WordsService) : KComposite() {

    private lateinit var words: MutableMap<String, WordExplanationData>
    private lateinit var grid: Grid<WordRowData>

    private val root = ui {
        verticalLayout {
            val horizonalLayout = addExplainInput()
            val searchLayoutInput = addSearchInput()
            searchLayoutInput.style.setMarginLeft("50px")
            horizonalLayout.add(searchLayoutInput)
            this.add(horizonalLayout)

            words = wordsService.getAllWordsWithContent()
            initWordsTable()
            updateWordsGridItems()
            this.add(grid)
        }
    }

    private fun showDetailDialog(wordExplanation: WordExplanationData) {
        val dialog = DetailsDialog(wordExplanation, wordsService)
        dialog.addClosedListener {
            if (dialog.isDataUpdated) {
                updateWordsGridItems()
            }
        }
        dialog.open()
    }

    private fun initWordsTable() {
        grid = Grid<WordRowData>()
        grid.addComponentColumn { rowData ->
            val wordDiv = Div()
            wordDiv.text = rowData.word
            wordDiv.addClickListener {
                showDetailDialog(words[rowData.word]!!)
            }
            wordDiv
        }.setHeader("Word").apply {
            flexGrow = 1
        }

        grid.addComponentColumn { rowData ->
            val translationDiv = Div()
            translationDiv.text = rowData.translation
            translationDiv.addClickListener {
                showDetailDialog(words[rowData.word]!!)
            }
            translationDiv
        }.setHeader("Translation").apply {
            flexGrow = 2
        }

        grid.addComponentColumn { rowData ->
            val deleteIcon = VaadinIcon.CLOSE_SMALL.create()
            deleteIcon.style.setColor("red")
            deleteIcon.addClickListener { event ->
                if (wordsService.deleteWord(rowData.word)) {
                    words.remove(rowData.word)
                    updateWordsGridItems()
                }
            }
            deleteIcon
        }.apply {
            flexGrow = 0
            width = "50px"
        }

        grid.height = "75vh"
    }

    private fun addExplainInput(): HorizontalLayout {
        val horizonalLayout = HorizontalLayout()
        val fieldName = TextField("Enter the word to explain")
        fieldName.className = "bordered"
        horizonalLayout.add(fieldName)
        val button = Button()
        button.text = "Explain"
        button.setPrimary()
        button.addClickShortcut(Key.ENTER).listenOn(fieldName)
        button.onLeftClick {
            if (fieldName.value.isNotBlank() && !wordsService.existsWord(fieldName.value)) {
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

    private fun addSearchInput(): HorizontalLayout {
        val horizonalLayout = HorizontalLayout()
        val fieldName = TextField("Search for a word")
        fieldName.className = "bordered"
        horizonalLayout.add(fieldName)
        val searchButton = Button()
        searchButton.text = "Search"
        searchButton.addClickShortcut(Key.ENTER).listenOn(fieldName)
        searchButton.onLeftClick {
            if (fieldName.value.isNotBlank()) {
                val searchResult = wordsService.getTranslationsFromExplanationDataList(
                    words.values.filter { it.word!!.contains(fieldName.value, ignoreCase = true) }
                )
                grid.setItems(searchResult)
            } else {
                updateWordsGridItems()
            }
        }

        val resetButton = Button("Reset") {
            fieldName.value = ""
            updateWordsGridItems()
        }
        horizonalLayout.add(searchButton, resetButton)
        horizonalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, searchButton)
        horizonalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, resetButton)
        return horizonalLayout
    }

    private fun updateWordsGridItems() {
        grid.setItems(
            wordsService.getTranslationsFromExplanationDataList(words.values)
        )
    }
}