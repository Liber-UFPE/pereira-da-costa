package br.ufpe.liber.search

import br.ufpe.liber.EagerInProduction
import br.ufpe.liber.services.BooksRepository
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.StoredField
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

@Singleton
@EagerInProduction
class Indexer(
    private val bookRepository: BooksRepository,
    private val directory: Directory,
    private val analyzer: Analyzer,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Indexer::class.java)
    }

    // This is optimized for fields that need highlighting
    private fun richTextField(name: String, content: String): Field {
        val fieldType = FieldType()
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
        fieldType.setStored(true)
        fieldType.setTokenized(true)
        fieldType.setOmitNorms(false)

        fieldType.setStoreTermVectors(true) // captures frequencies
        fieldType.setStoreTermVectorPositions(true) // depends on freqs
        fieldType.setStoreTermVectorOffsets(true) // depends on freqs

        return Field(name, content, fieldType)
    }

    init {
        logger.info("Starting to index all books")
        IndexWriter(directory, IndexWriterConfig(analyzer)).use { writer ->
            bookRepository.listAll().parallelStream().forEach { book ->
                val time = measureTimeMillis {
                    book.pages.forEach { page ->
                        val doc = Document()

                        // Book fields
                        doc.add(StoredField(BookMetadata.NUMBER, book.number))

                        // Page fields
                        doc.add(StoredField(PageMetadata.NUMBER, page.number))
                        doc.add(StoredField(PageMetadata.YEAR, page.year))
                        doc.add(richTextField(PageMetadata.TEXT, page.text))
                        writer.addDocument(doc)
                    }
                }
                logger.info("Book ${book.number} indexed in $time ms")
            }
        }
        logger.info("All books indexed")
    }
}
