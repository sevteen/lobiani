package dev.baybay.lobiani.app.inventory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryItemAPISpec extends Specification {

    private static final URI = "/api/inventory-items"
    private static final SLUG = "the-matrix-trilogy"
    private static final int AXON_SERVER_HTTP_PORT = 8024
    private static final int AXON_SERVER_GRPC_PORT = 8124

    @Autowired
    TestRestTemplate restTemplate

    @Shared
    GenericContainer container = new GenericContainer("axoniq/axonserver:4.4")
            .withExposedPorts(AXON_SERVER_HTTP_PORT, AXON_SERVER_GRPC_PORT)
            .waitingFor(Wait.forHttp("/actuator/info").forPort(AXON_SERVER_HTTP_PORT))
            .withStartupTimeout(Duration.ofSeconds(60))

    def id

    void setupSpec() {
        def port = container.getMappedPort(AXON_SERVER_GRPC_PORT)
        System.setProperty("axon.axonserver.servers", "${container.getHost()}:$port")
    }

    void cleanup() {
        id && deleteItem(id)
    }

    def "item is defined"() {
        when:
        def response = defineItem()

        then:
        response.statusCode.is2xxSuccessful()
        assertItem(response.body)
    }

    def "defined item is retrieved"() {
        given:
        itemDefined()

        when:
        def response = getItemEntity(id)

        then:
        response.statusCode.is2xxSuccessful()
        assertItem(response.body)
    }

    def "NotFound is returned when item isn't defined"() {
        given:
        def undefinedItemId = UUID.randomUUID()

        when:
        def response = getItemEntity(undefinedItemId)

        then:
        response.statusCode == HttpStatus.NOT_FOUND
    }

    def "defined items are retrieved"() {
        given:
        itemDefined()

        when:
        def response = getItemsEntity()
        def items = response.body

        then:
        response.statusCode.is2xxSuccessful()
        items.size() == 1
        assertItem(items[0])
    }

    def "empty result is returned when no items are defined"() {
        when:
        def response = getItemsEntity()

        then:
        response.statusCode.is2xxSuccessful()
        response.body.empty
    }

    def "defined item is deleted"() {
        given:
        itemDefined()

        when:
        deleteItem(id)

        then:
        getItemEntity(id).statusCode == HttpStatus.NOT_FOUND
    }

    def "NotFound is returned when deleting undefined item"() {
        given:
        def undefinedItemId = UUID.randomUUID()

        when:
        def response = deleteItem(undefinedItemId)

        then:
        response.statusCode == HttpStatus.NOT_FOUND
    }

    def "item is added to stock"() {
        given:
        itemDefined()

        when:
        def response = addItemToStock(10)

        then:
        response.statusCode.is2xxSuccessful()

        when:
        response = getItemEntity(id)

        then:
        response.statusCode.is2xxSuccessful()
        response.body.stockLevel == 10
    }

    @Unroll
    def "BadRequest is returned when #amount items are added to stock"() {
        given:
        itemDefined()

        when:
        def response = addItemToStock(amount)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.message == "Amount must be a positive number"

        where:
        amount << [0, -10]
    }

    def "BadRequest is returned when item with same slug is already defined"() {
        given:
        itemDefined()

        when:
        def response = defineItem()

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.message == "Item with slug $SLUG is already defined"
    }

    ResponseEntity<Object> defineItem() {
        def response = restTemplate.postForEntity(URI, [slug: SLUG], Object)
        if (response.body.id) {
            id = response.body.id
        }
        return response
    }

    def itemDefined() {
        defineItem()
    }

    ResponseEntity<Object> getItemEntity(id) {
        restTemplate.getForEntity("$URI/$id", Object)
    }

    ResponseEntity<List> getItemsEntity() {
        restTemplate.getForEntity(URI, List)
    }

    ResponseEntity<Object> deleteItem(id) {
        restTemplate.exchange("$URI/$id", HttpMethod.DELETE, null, Object)
    }

    ResponseEntity<Object> addItemToStock(amount) {
        restTemplate.postForEntity("$URI/$id/stock", [amount: amount], Object)
    }

    static void assertItem(item) {
        assert item.id != null
        assert item.slug == SLUG
        assert item.stockLevel == 0
    }
}
