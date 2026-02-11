package com.storozhuk.translator.ui

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.horizontalAlignSelf
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalAlignSelf
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
import com.vaadin.flow.component.html.Image
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
            val row = HorizontalLayout()
            val meaning = UnorderedList()
            val meaningAndUsageItems = ListItem(Div(definition.meaning), Div(definition.usage))
            meaning.add(meaningAndUsageItems)
            val example = UnorderedList()
            example.add(ListItem("Example: ${definition.example!!.foreignLanguage}"))
            example.add(ListItem("Original: ${definition.example.english}"))
            meaning.add(example)

            val deleteDefinitionBtn = Image("img/red_cross.jpg", "Delete definition")
            deleteDefinitionBtn.width = "35px"
            deleteDefinitionBtn.height = "35px"
            deleteDefinitionBtn.element.addEventListener("mouseover") {
                deleteDefinitionBtn.style.setBackgroundColor("#ededed")
            }
            deleteDefinitionBtn.element.addEventListener("mouseout") {
                deleteDefinitionBtn.style.setBackgroundColor("transparent")
            }
            deleteDefinitionBtn.addClickListener {
                wordExplanation.definitions!!.remove(definition)
                definitions.remove(row)
            }
            meaning.style.setMarginBottom("15px")
            meaning.element.addEventListener("mouseover") {
                meaning.style.setBackgroundColor("#ededed")
            }
            meaning.element.addEventListener("mouseout") {
                meaning.style.setBackgroundColor("transparent")
            }
            row.add(meaning, deleteDefinitionBtn)
            definitions.add(row)
        }
        val addBtnImage = Image("img/add_btn.png", "Add translation")
        addBtnImage.width = "40px"
        addBtnImage.height = "40px"
        addBtnImage.element.addEventListener("mouseover") {
            addBtnImage.style.setBackgroundColor("#ededed")
        }
        addBtnImage.element.addEventListener("mouseout") {
            addBtnImage.style.setBackgroundColor("transparent")
        }
        addBtnImage.addClickListener {
            AddTranslationDialog(wordExplanation, wordsService).open()
        }
        definitions.add(addBtnImage)
        content.add(definitions)

        val closeButton = Button("Close") {
            wordsService.updateWord(wordExplanation)
            dialog.close()
        }
        dialog.addClosedListener {
            wordsService.updateWord(wordExplanation)
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