package org.acme;

import dev.langchain4j.data.image.Image;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.ai.AiImageProcessingService;
import org.acme.ai.AiItemCategorizationService;
import org.acme.model.Product;

import java.util.Base64;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ItemRecognition {

    @Inject
    AiImageProcessingService imageProcessingService;

    @Inject
    AiItemCategorizationService categorizationService;

    @Channel("process-item")
    Emitter<ItemCategoryDto> newItemProcessed;

    @Inject
    Logger logger;

    public void detectProduct(byte[] image, String mime) {
        String base64Image = Base64.getEncoder().encodeToString(image);
        Image encodedImage = Image.builder().base64Data(base64Image).mimeType(mime).build();

        /**Uni<Item> item = imageProcessingService.extractInfo(encodedImage);
        item.subscribe()
            .with(i -> {
                categorizationService.categorize(i)
                    .onItem().transform(ic -> {
                        ItemCategoryDto itemCategoryDto = new ItemCategoryDto();
                        itemCategoryDto.description = i.description;
                        itemCategoryDto.brand = i.brand;
                        itemCategoryDto.label = i.label;
                        itemCategoryDto.condition = i.condition;
                        itemCategoryDto.model = i.model;
                        itemCategoryDto.price = i.price;

                        itemCategoryDto.category = ic.category;
                        itemCategoryDto.subcategory = ic.subcategory;

                        return itemCategoryDto;
                    }).subscribe()
                    .with(icdto -> {
                        logger.infof("Detected %s %s", icdto.brand, icdto.model);
                        newItemProcessed.send(icdto);
                    });
            });*/
        imageProcessingService.extractInfo(encodedImage)
            .onItem().transformToUni(item ->
                categorizationService.categorize(item)
                    .onItem().transform(ic -> {
                        ItemCategoryDto dto = new ItemCategoryDto();
                        dto.description = item.description;
                        dto.brand = item.brand;
                        dto.label = item.label;
                        dto.condition = item.condition;
                        dto.model = item.model;
                        dto.price = item.price;

                        dto.category = ic.category;
                        dto.subcategory = ic.subcategory;
                        return dto;
                    })
            )
            .subscribe()
            .with(dto -> {
                logger.infof("Detected %s %s", dto.brand, dto.model);
                newItemProcessed.send(dto);
            });

    }

}
