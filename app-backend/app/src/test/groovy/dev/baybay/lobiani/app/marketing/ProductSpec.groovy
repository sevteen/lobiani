package dev.baybay.lobiani.app.marketing

import dev.baybay.lobiani.app.common.Slug
import dev.baybay.lobiani.app.marketing.command.DefineProduct
import dev.baybay.lobiani.app.marketing.command.DeleteProduct
import dev.baybay.lobiani.app.marketing.event.ProductDefined
import dev.baybay.lobiani.app.marketing.event.ProductDeleted
import dev.baybay.lobiani.testutil.AggregateSpec

class ProductSpec extends AggregateSpec {

    def id = new ProductIdentifier(UUID.randomUUID())
    def slug = new Slug("the-matrix-trilogy")
    def title = "The Matrix Trilogy"
    def description = "This is Matrix"

    void setup() {
        useAggregate Product
    }

    def "should define new product"() {
        when:
        actingWith new DefineProduct(id, slug, title, description)

        then:
        expectSuccess()

        and:
        expectEvent new ProductDefined(id, slug, title, description)
    }

    def "should delete defined product"() {
        given:
        pastEvent new ProductDefined(id, slug, title, description)

        when:
        actingWith new DeleteProduct(id)

        then:
        expectSuccess()

        and:
        expectEvent new ProductDeleted(id)
    }

    def "ProductIdentifier.toString() should return UUID"() {
        expect:
        id.toString() == id.value.toString()
    }
}
