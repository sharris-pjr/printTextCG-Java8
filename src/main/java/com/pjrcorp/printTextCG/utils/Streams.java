package com.pjrcorp.printTextCG.utils;


import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams
{

    private Streams()
    {
    }

    public interface ConsumerWithException<T, E extends Exception>
    {
        void accept(T t) throws E;
    }

    private static class WrappedException extends RuntimeException
    {
        private static final long serialVersionUID = -4788418092253878703L;

        WrappedException(Exception e)
        {
            super(e);
        }
    }

    /**
     * Runs stream.foreach with a consumer that throws at most one checked
     * exception type.
     * 
     * @param stream
     *            The stream to process.
     * @param consumer
     *            The consumer of the stream items.
     * @param exceptionClass
     *            The exception class of which consumer may throw
     * @throws E
     *             if consumer causes an exception.
     */
    public static <T, E extends Exception> void forEachStream(Stream<T> stream, ConsumerWithException<T, E> consumer,
            Class<E> exceptionClass) throws E
    {
        try
        {
            stream.forEach(t -> {
                try
                {
                    consumer.accept(t);
                } catch (RuntimeException e)
                {
                    throw e;
                } catch (Exception exception)
                {
                    if (exceptionClass.isInstance(exception))
                    {
                        throw new WrappedException(exception);
                    }
                    throw new RuntimeException(exception);
                }
            });
        } catch (WrappedException e)
        {
            throw exceptionClass.cast(e.getCause());
        }
    }

    /**
     * Generically turns the iterator into an ordered non-parallel stream. 
     * 
     * This function exists primarily to enhances readability of the code.
     * 
     * @param iterator
     *            The iterator
     * @return A stream backed by this iterator.
     */
    public static <T> Stream<T> fromIterator(Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
