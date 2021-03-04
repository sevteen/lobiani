package dev.baybay.lobiani.app.marketing.upcasting


import dev.baybay.lobiani.app.marketing.event.ProductDeleted
import dev.baybay.lobiani.app.marketing.ProductIdentifier
import dev.baybay.lobiani.testutil.UpcasterSpec

class ProductDeletedUpcasterTest extends UpcasterSpec {

    void setup() {
        use new ProductDeletedNullTo1Upcaster()
    }

    def "should upcast null to 1"() {
        given:
        def id = "1c883d6b-e484-488a-b7ba-ff0886cbd452"
        def nullRevisionSerializedPayload = """
        <dev.baybay.lobiani.app.product.api.ProductDeleted>
            <id>$id</id>
        </dev.baybay.lobiani.app.product.api.ProductDeleted>
        """
        def oldRevision = null
        def serializedEvent = new StringSerializedObject(
                nullRevisionSerializedPayload,
                ProductDeletedNullTo1Upcaster.SERIALIZED_OBJECT_TYPE,
                oldRevision
        )

        when:
        ProductDeleted upcasted = upcastSinglePayload serializedEvent

        then:
        upcasted.id == new ProductIdentifier(UUID.fromString(id))
    }
}