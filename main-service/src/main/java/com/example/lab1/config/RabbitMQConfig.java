@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue importQueue() {
        return new Queue("import-queue", true);
    }

    @Bean
    public TopicExchange importExchange() {
        return new TopicExchange("import-exchange");
    }

    @Bean
    public Binding binding(Queue importQueue, TopicExchange importExchange) {
        return BindingBuilder.bind(importQueue)
                .to(importExchange)
                .with("import");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}