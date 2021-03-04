package dev.baybay.lobiani.app.inventory

import dev.baybay.lobiani.app.inventory.event.*
import dev.baybay.lobiani.app.inventory.command.AddInventoryItemToStock
import dev.baybay.lobiani.app.inventory.command.DefineInventoryItem
import dev.baybay.lobiani.app.inventory.command.DeleteInventoryItem
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateLifecycle.markDeleted
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class InventoryItem {

    @AggregateIdentifier
    private lateinit var id: UUID
    private lateinit var slug: String

    constructor()

    @CommandHandler
    constructor(c: DefineInventoryItem) {
        apply(InventoryItemDefined(c.id, c.slug.value))
    }

    @CommandHandler
    fun handle(c: DeleteInventoryItem) {
        apply(InventoryItemDeleted(id, slug))
    }

    @CommandHandler
    fun handle(c: AddInventoryItemToStock) {
        apply(InventoryItemAddedToStock(c.inventoryItemId, c.quantity))
    }

    @EventSourcingHandler
    fun on(e: InventoryItemDefined) {
        id = e.id
        slug = e.slug
    }

    @EventSourcingHandler
    fun on(e: InventoryItemDeleted) {
        markDeleted()
    }
}