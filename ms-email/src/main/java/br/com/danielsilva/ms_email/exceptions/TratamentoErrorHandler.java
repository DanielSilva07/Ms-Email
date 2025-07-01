package br.com.danielsilva.ms_email.exceptions;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.util.ErrorHandler;

public class TratamentoErrorHandler implements ErrorHandler  {
    @Override
    public void handleError(Throwable t) {

        String nomeDaFila = ((ListenerExecutionFailedException) t).getFailedMessage()
                .getMessageProperties().getConsumerQueue();
        System.out.println(nomeDaFila);

        String message = new String(((ListenerExecutionFailedException) t).getFailedMessage().getBody());
        System.out.println(message);

        System.out.println(t.getCause().getMessage());

        throw new AmqpRejectAndDontRequeueException("Ocorreu um erro ao processar a mensagem");
    }

}

