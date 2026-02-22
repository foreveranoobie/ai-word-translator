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
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.ListItem
import com.vaadin.flow.component.html.UnorderedList
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
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
            meaning.addClassName("ul-main")

            val deleteDefinitionBtn = Image("img/red_cross.jpg", "Delete definition")
            deleteDefinitionBtn.width = "20px"
            deleteDefinitionBtn.height = "20px"
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

            val buttonWrapper = Div(deleteDefinitionBtn)
            buttonWrapper.style.setMarginLeft("auto")

            meaning.style.setMarginBottom("15px")
            row.add(meaning, buttonWrapper)
            row.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, buttonWrapper)
            definitions.add(row)
        }
        definitions.addClassName("custom-layout-grid")


        val addBtnImage = Image("img/add_btn.png", "Add translation")
        addBtnImage.width = "25px"
        addBtnImage.height = "25px"

        val addBtn = Button("Add explanation") {
            val translationDialog = AddTranslationDialog(wordExplanation, wordsService)
            translationDialog.addClassName("popup-dialog")
            translationDialog.open()
        }
        addBtn.addClassName("add-explanation-button")
        addBtn.icon = Icon(VaadinIcon.PLUS_CIRCLE)
        content.addClassName("explanation-layout")
        content.add(definitions, addBtn)

        val closeButton = Button("Close") {
            dialog.close()
        }
        dialog.addClosedListener {
            wordsService.updateWord(wordExplanation)
        }

        dialog.add(content)
        dialog.footer.add(closeButton)
        dialog.addClassName("popup-dialog")

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
        //searchButton.height = "50%"

        val resetButton = Button("Reset") {
            fieldName.value = ""
            updateWordsGridItems()
        }
       //resetButton.height = "50%"
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