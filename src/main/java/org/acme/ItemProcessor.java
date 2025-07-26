package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Product;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ItemProcessor {

    @Inject
    Logger logger;

    @Incoming("process-item")
    public void processedItemAndNotify(ItemCategoryDto itemCategoryDto) {
        logger.infof("Processing %s %s", itemCategoryDto.brand, itemCategoryDto.model);
        this.insertProduct(itemCategoryDto);
    }

    @Transactional
    public Product insertProduct(ItemCategoryDto item) {
        Product product = new Product();
        product.description = item.description;
        product.brand = item.brand;
        product.label = item.label;
        product.condition = item.condition;
        product.model = item.model;
        product.price = item.price;

        product.category = item.category;
        product.subcategory = item.subcategory;

        product.persist();

        return product;
    }

}
