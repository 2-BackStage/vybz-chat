package back.vybz.chat_service.common.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoCappedCollectionConfig {

    @Bean
    public ApplicationRunner initCappedCollection(MongoTemplate mongoTemplate) {
        return args -> {
            String collectionName = "chat_message";

            if (!mongoTemplate.collectionExists(collectionName)) {
                mongoTemplate.createCollection(
                        collectionName,
                        CollectionOptions.empty()
                                .capped()
                                .size(5242880)  // 5MB
                                .maxDocuments(10000)
                );
            }
        };
    }

}
