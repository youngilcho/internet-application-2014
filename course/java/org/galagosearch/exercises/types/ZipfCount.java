// This file was automatically generated with the command: 
//     java org.galagosearch.tupleflow.typebuilder.TypeBuilderMojo ...

// you should modify package path of ZipfCount that you made
package org.galagosearch.exercises.types;

import org.galagosearch.tupleflow.Utility;
import org.galagosearch.tupleflow.ArrayInput;
import org.galagosearch.tupleflow.ArrayOutput;
import org.galagosearch.tupleflow.Order;   
import org.galagosearch.tupleflow.OrderedWriter;
import org.galagosearch.tupleflow.Type; 
import org.galagosearch.tupleflow.TypeReader;
import org.galagosearch.tupleflow.Step; 
import org.galagosearch.tupleflow.IncompatibleProcessorException;
import org.galagosearch.tupleflow.ReaderSource;
import java.io.IOException;             
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;   
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Collection;

public class ZipfCount implements Type<ZipfCount> {
    public long uniqueWords;
    public long occurrenceCount; 
    
    public ZipfCount() {}
    public ZipfCount(long uniqueWords, long occurrenceCount) {
        this.uniqueWords = uniqueWords;
        this.occurrenceCount = occurrenceCount;
    }  
    
    public String toString() {
            return String.format("%d,%d",
                                   uniqueWords, occurrenceCount);
    } 

    public Order<ZipfCount> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] { "+occurrenceCount" })) {
            return new OccurrenceCountOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.galagosearch.tupleflow.Processor<ZipfCount> {
        public void process(ZipfCount object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class OccurrenceCountOrder implements Order<ZipfCount> {
        public int hash(ZipfCount object) {
            int h = 0;
            h += Utility.hash(object.occurrenceCount);
            return h;
        } 
        public Comparator<ZipfCount> greaterThan() {
            return new Comparator<ZipfCount>() {
                public int compare(ZipfCount one, ZipfCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.occurrenceCount, two.occurrenceCount);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<ZipfCount> lessThan() {
            return new Comparator<ZipfCount>() {
                public int compare(ZipfCount one, ZipfCount two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.occurrenceCount, two.occurrenceCount);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<ZipfCount> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<ZipfCount> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<ZipfCount> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< ZipfCount > {
            ZipfCount last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(ZipfCount object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.occurrenceCount, last.occurrenceCount)) { processAll = true; shreddedWriter.processOccurrenceCount(object.occurrenceCount); }
               shreddedWriter.processTuple(object.uniqueWords);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<ZipfCount> getInputClass() {
                return ZipfCount.class;
            }
        } 
        public ReaderSource<ZipfCount> orderedCombiner(Collection<TypeReader<ZipfCount>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<ZipfCount> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public ZipfCount clone(ZipfCount object) {
            ZipfCount result = new ZipfCount();
            if (object == null) return result;
            result.uniqueWords = object.uniqueWords; 
            result.occurrenceCount = object.occurrenceCount; 
            return result;
        }                 
        public Class<ZipfCount> getOrderedClass() {
            return ZipfCount.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+occurrenceCount"};
        }

        public static String getSpecString() {
            return "+occurrenceCount";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processOccurrenceCount(long occurrenceCount) throws IOException;
            public void processTuple(long uniqueWords) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            long lastOccurrenceCount;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processOccurrenceCount(long occurrenceCount) {
                lastOccurrenceCount = occurrenceCount;
                buffer.processOccurrenceCount(occurrenceCount);
            }
            public final void processTuple(long uniqueWords) throws IOException {
                if (lastFlush) {
                    if(buffer.occurrenceCounts.size() == 0) buffer.processOccurrenceCount(lastOccurrenceCount);
                    lastFlush = false;
                }
                buffer.processTuple(uniqueWords);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeLong(buffer.getUniqueWords());
                    buffer.incrementTuple();
                }
            }  
            public final void flushOccurrenceCount(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getOccurrenceCountEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeLong(buffer.getOccurrenceCount());
                    output.writeInt(count);
                    buffer.incrementOccurrenceCount();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushOccurrenceCount(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<Long> occurrenceCounts = new ArrayList();
            ArrayList<Integer> occurrenceCountTupleIdx = new ArrayList();
            int occurrenceCountReadIdx = 0;
                            
            long[] uniqueWordss;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                uniqueWordss = new long[batchSize];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processOccurrenceCount(long occurrenceCount) {
                occurrenceCounts.add(occurrenceCount);
                occurrenceCountTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(long uniqueWords) {
                assert occurrenceCounts.size() > 0;
                uniqueWordss[writeTupleIndex] = uniqueWords;
                writeTupleIndex++;
            }
            public void resetData() {
                occurrenceCounts.clear();
                occurrenceCountTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                occurrenceCountReadIdx = 0;
            } 

            public void reset() {
                resetData();
                resetRead();
            } 
            public boolean isFull() {
                return writeTupleIndex >= batchSize;
            }

            public boolean isEmpty() {
                return writeTupleIndex == 0;
            }                          

            public boolean isAtEnd() {
                return readTupleIndex >= writeTupleIndex;
            }           
            public void incrementOccurrenceCount() {
                occurrenceCountReadIdx++;  
            }                                                                                              

            public void autoIncrementOccurrenceCount() {
                while (readTupleIndex >= getOccurrenceCountEndIndex() && readTupleIndex < writeTupleIndex)
                    occurrenceCountReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getOccurrenceCountEndIndex() {
                if ((occurrenceCountReadIdx+1) >= occurrenceCountTupleIdx.size())
                    return writeTupleIndex;
                return occurrenceCountTupleIdx.get(occurrenceCountReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public long getOccurrenceCount() {
                assert readTupleIndex < writeTupleIndex;
                assert occurrenceCountReadIdx < occurrenceCounts.size();
                
                return occurrenceCounts.get(occurrenceCountReadIdx);
            }
            public long getUniqueWords() {
                assert readTupleIndex < writeTupleIndex;
                return uniqueWordss[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getUniqueWords());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexOccurrenceCount(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processOccurrenceCount(getOccurrenceCount());
                    assert getOccurrenceCountEndIndex() <= endIndex;
                    copyTuples(getOccurrenceCountEndIndex(), output);
                    incrementOccurrenceCount();
                }
            }  
            public void copyUntilOccurrenceCount(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getOccurrenceCount(), other.getOccurrenceCount());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processOccurrenceCount(getOccurrenceCount());
                                      
                        copyTuples(getOccurrenceCountEndIndex(), output);
                    } else {
                        output.processOccurrenceCount(getOccurrenceCount());
                        copyTuples(getOccurrenceCountEndIndex(), output);
                    }
                    incrementOccurrenceCount();  
                    
               
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilOccurrenceCount(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<ZipfCount>, ShreddedSource {   
            public ShreddedProcessor processor;
            Collection<ShreddedReader> readers;       
            boolean closeOnExit = false;
            boolean uninitialized = true;
            PriorityQueue<ShreddedReader> queue = new PriorityQueue<ShreddedReader>();
            
            public ShreddedCombiner(Collection<ShreddedReader> readers, boolean closeOnExit) {
                this.readers = readers;                                                       
                this.closeOnExit = closeOnExit;
            }
                                  
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof ZipfCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((ZipfCount.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<ZipfCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<ZipfCount> getOutputClass() {
                return ZipfCount.class;
            }
            
            public void initialize() throws IOException {
                for (ShreddedReader reader : readers) {
                    reader.fill();                                        
                    
                    if (!reader.getBuffer().isAtEnd())
                        queue.add(reader);
                }   

                uninitialized = false;
            }

            public void run() throws IOException {
                initialize();
               
                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    ShreddedReader next = null;
                    ShreddedBuffer nextBuffer = null; 
                    
                    assert !top.getBuffer().isAtEnd();
                                                  
                    if (queue.size() > 0) {
                        next = queue.peek();
                        nextBuffer = next.getBuffer();
                        assert !nextBuffer.isAtEnd();
                    }
                    
                    top.getBuffer().copyUntil(nextBuffer, processor);
                    if (top.getBuffer().isAtEnd())
                        top.fill();                 
                        
                    if (!top.getBuffer().isAtEnd())
                        queue.add(top);
                }              
                
                if (closeOnExit)
                    processor.close();
            }

            public ZipfCount read() throws IOException {
                if (uninitialized)
                    initialize();

                ZipfCount result = null;

                while (queue.size() > 0) {
                    ShreddedReader top = queue.poll();
                    result = top.read();

                    if (result != null) {
                        if (top.getBuffer().isAtEnd())
                            top.fill();

                        queue.offer(top);
                        break;
                    } 
                }

                return result;
            }
        } 
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<ZipfCount>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            ZipfCount last = new ZipfCount();         
            long updateOccurrenceCountCount = -1;
            long tupleCount = 0;
            long bufferStartCount = 0;  
            ArrayInput input;
            
            public ShreddedReader(ArrayInput input) {
                this.input = input; 
                this.buffer = new ShreddedBuffer();
            }                               
            
            public ShreddedReader(ArrayInput input, int bufferSize) { 
                this.input = input;
                this.buffer = new ShreddedBuffer(bufferSize);
            }
                 
            public final int compareTo(ShreddedReader other) {
                ShreddedBuffer otherBuffer = other.getBuffer();
                
                if (buffer.isAtEnd() && otherBuffer.isAtEnd()) {
                    return 0;                 
                } else if (buffer.isAtEnd()) {
                    return -1;
                } else if (otherBuffer.isAtEnd()) {
                    return 1;
                }
                                   
                int result = 0;
                do {
                    result = + Utility.compare(buffer.getOccurrenceCount(), otherBuffer.getOccurrenceCount());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final ZipfCount read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                ZipfCount result = new ZipfCount();
                
                result.occurrenceCount = buffer.getOccurrenceCount();
                result.uniqueWords = buffer.getUniqueWords();
                
                buffer.incrementTuple();
                buffer.autoIncrementOccurrenceCount();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateOccurrenceCountCount - tupleCount > 0) {
                            buffer.occurrenceCounts.add(last.occurrenceCount);
                            buffer.occurrenceCountTupleIdx.add((int) (updateOccurrenceCountCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateOccurrenceCount();
                        buffer.processTuple(input.readLong());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateOccurrenceCount() throws IOException {
                if (updateOccurrenceCountCount > tupleCount)
                    return;
                     
                last.occurrenceCount = input.readLong();
                updateOccurrenceCountCount = tupleCount + input.readInt();
                                      
                buffer.processOccurrenceCount(last.occurrenceCount);
            }

            public void run() throws IOException {
                while (true) {
                    fill();
                    
                    if (buffer.isAtEnd())
                        break;
                    
                    buffer.copyUntil(null, processor);
                }      
                processor.close();
            }
            
            public void setProcessor(Step processor) throws IncompatibleProcessorException {  
                if (processor instanceof ShreddedProcessor) {
                    this.processor = new DuplicateEliminator((ShreddedProcessor) processor);
                } else if (processor instanceof ZipfCount.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((ZipfCount.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<ZipfCount>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<ZipfCount> getOutputClass() {
                return ZipfCount.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            ZipfCount last = new ZipfCount();
            boolean occurrenceCountProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processOccurrenceCount(long occurrenceCount) throws IOException {  
                if (occurrenceCountProcess || Utility.compare(occurrenceCount, last.occurrenceCount) != 0) {
                    last.occurrenceCount = occurrenceCount;
                    processor.processOccurrenceCount(occurrenceCount);
                    occurrenceCountProcess = false;
                }
            }  
            
            public void resetOccurrenceCount() {
                 occurrenceCountProcess = true;
            }                                                
                               
            public void processTuple(long uniqueWords) throws IOException {
                processor.processTuple(uniqueWords);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            ZipfCount last = new ZipfCount();
            public org.galagosearch.tupleflow.Processor<ZipfCount> processor;                               
            
            public TupleUnshredder(ZipfCount.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.galagosearch.tupleflow.Processor<ZipfCount> processor) {
                this.processor = processor;
            }
            
            public ZipfCount clone(ZipfCount object) {
                ZipfCount result = new ZipfCount();
                if (object == null) return result;
                result.uniqueWords = object.uniqueWords; 
                result.occurrenceCount = object.occurrenceCount; 
                return result;
            }                 
            
            public void processOccurrenceCount(long occurrenceCount) throws IOException {
                last.occurrenceCount = occurrenceCount;
            }   
                
            
            public void processTuple(long uniqueWords) throws IOException {
                last.uniqueWords = uniqueWords;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            ZipfCount last = new ZipfCount();
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public ZipfCount clone(ZipfCount object) {
                ZipfCount result = new ZipfCount();
                if (object == null) return result;
                result.uniqueWords = object.uniqueWords; 
                result.occurrenceCount = object.occurrenceCount; 
                return result;
            }                 
            
            public void process(ZipfCount object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.occurrenceCount, object.occurrenceCount) != 0 || processAll) { processor.processOccurrenceCount(object.occurrenceCount); processAll = true; }
                processor.processTuple(object.uniqueWords);                                         
            }
                          
            public Class<ZipfCount> getInputClass() {
                return ZipfCount.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    