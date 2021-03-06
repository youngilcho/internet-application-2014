// This file was automatically generated with the command: 
//     java org.galagosearch.tupleflow.typebuilder.TypeBuilderMojo ...
package org.galagosearch.core.types;

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

public class DocumentSplit implements Type<DocumentSplit> {
    public String fileName;
    public String fileType;
    public boolean isCompressed;
    public byte[] startKey;
    public byte[] endKey; 
    
    public DocumentSplit() {}
    public DocumentSplit(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.isCompressed = isCompressed;
        this.startKey = startKey;
        this.endKey = endKey;
    }  
    
    public String toString() {
        try {
            return String.format("%s,%s,%b,%s,%s",
                                   fileName, fileType, isCompressed, new String(startKey, "UTF-8"), new String(endKey, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert string to UTF-8.");
        }
    } 

    public Order<DocumentSplit> getOrder(String... spec) {
        if (Arrays.equals(spec, new String[] {  })) {
            return new Unordered();
        }
        if (Arrays.equals(spec, new String[] { "+fileName", "+startKey" })) {
            return new FileNameStartKeyOrder();
        }
        return null;
    } 
      
    public interface Processor extends Step, org.galagosearch.tupleflow.Processor<DocumentSplit> {
        public void process(DocumentSplit object) throws IOException;
        public void close() throws IOException;
    }                        
    public interface Source extends Step {
    }
    public static class Unordered implements Order<DocumentSplit> {
        public int hash(DocumentSplit object) {
            int h = 0;
            return h;
        } 
        public Comparator<DocumentSplit> greaterThan() {
            return new Comparator<DocumentSplit>() {
                public int compare(DocumentSplit one, DocumentSplit two) {
                    int result = 0;
                    do {
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentSplit> lessThan() {
            return new Comparator<DocumentSplit>() {
                public int compare(DocumentSplit one, DocumentSplit two) {
                    int result = 0;
                    do {
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentSplit> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentSplit> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentSplit> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentSplit > {
            DocumentSplit last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentSplit object) throws IOException {
               boolean processAll = false;
               shreddedWriter.processTuple(object.fileName, object.fileType, object.isCompressed, object.startKey, object.endKey);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentSplit> getInputClass() {
                return DocumentSplit.class;
            }
        } 
        public ReaderSource<DocumentSplit> orderedCombiner(Collection<TypeReader<DocumentSplit>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentSplit> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentSplit clone(DocumentSplit object) {
            DocumentSplit result = new DocumentSplit();
            if (object == null) return result;
            result.fileName = object.fileName; 
            result.fileType = object.fileType; 
            result.isCompressed = object.isCompressed; 
            result.startKey = object.startKey; 
            result.endKey = object.endKey; 
            return result;
        }                 
        public Class<DocumentSplit> getOrderedClass() {
            return DocumentSplit.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {};
        }

        public static String getSpecString() {
            return "";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processTuple(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public final void processTuple(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) throws IOException {
                if (lastFlush) {
                    lastFlush = false;
                }
                buffer.processTuple(fileName, fileType, isCompressed, startKey, endKey);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getFileName());
                    output.writeString(buffer.getFileType());
                    output.writeBoolean(buffer.getIsCompressed());
                    output.writeBytes(buffer.getStartKey());
                    output.writeBytes(buffer.getEndKey());
                    buffer.incrementTuple();
                }
            }  
            public void flush() throws IOException { 
                flushTuples(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
                            
            String[] fileNames;
            String[] fileTypes;
            boolean[] isCompresseds;
            byte[][] startKeys;
            byte[][] endKeys;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                fileNames = new String[batchSize];
                fileTypes = new String[batchSize];
                isCompresseds = new boolean[batchSize];
                startKeys = new byte[batchSize][];
                endKeys = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processTuple(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) {
                fileNames[writeTupleIndex] = fileName;
                fileTypes[writeTupleIndex] = fileType;
                isCompresseds[writeTupleIndex] = isCompressed;
                startKeys[writeTupleIndex] = startKey;
                endKeys[writeTupleIndex] = endKey;
                writeTupleIndex++;
            }
            public void resetData() {
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
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
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getFileName() {
                assert readTupleIndex < writeTupleIndex;
                return fileNames[readTupleIndex];
            }                                         
            public String getFileType() {
                assert readTupleIndex < writeTupleIndex;
                return fileTypes[readTupleIndex];
            }                                         
            public boolean getIsCompressed() {
                assert readTupleIndex < writeTupleIndex;
                return isCompresseds[readTupleIndex];
            }                                         
            public byte[] getStartKey() {
                assert readTupleIndex < writeTupleIndex;
                return startKeys[readTupleIndex];
            }                                         
            public byte[] getEndKey() {
                assert readTupleIndex < writeTupleIndex;
                return endKeys[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFileName(), getFileType(), getIsCompressed(), getStartKey(), getEndKey());
                   incrementTuple();
                }
            }                                                                           
             
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentSplit>, ShreddedSource {   
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
                } else if (processor instanceof DocumentSplit.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentSplit.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<DocumentSplit>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentSplit> getOutputClass() {
                return DocumentSplit.class;
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

            public DocumentSplit read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentSplit result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentSplit>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentSplit last = new DocumentSplit();         
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
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentSplit read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentSplit result = new DocumentSplit();
                
                result.fileName = buffer.getFileName();
                result.fileType = buffer.getFileType();
                result.isCompressed = buffer.getIsCompressed();
                result.startKey = buffer.getStartKey();
                result.endKey = buffer.getEndKey();
                
                buffer.incrementTuple();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        buffer.processTuple(input.readString(), input.readString(), input.readBoolean(), input.readBytes(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
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
                } else if (processor instanceof DocumentSplit.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentSplit.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<DocumentSplit>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentSplit> getOutputClass() {
                return DocumentSplit.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentSplit last = new DocumentSplit();
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

          
            
                               
            public void processTuple(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) throws IOException {
                processor.processTuple(fileName, fileType, isCompressed, startKey, endKey);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentSplit last = new DocumentSplit();
            public org.galagosearch.tupleflow.Processor<DocumentSplit> processor;                               
            
            public TupleUnshredder(DocumentSplit.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.galagosearch.tupleflow.Processor<DocumentSplit> processor) {
                this.processor = processor;
            }
            
            public DocumentSplit clone(DocumentSplit object) {
                DocumentSplit result = new DocumentSplit();
                if (object == null) return result;
                result.fileName = object.fileName; 
                result.fileType = object.fileType; 
                result.isCompressed = object.isCompressed; 
                result.startKey = object.startKey; 
                result.endKey = object.endKey; 
                return result;
            }                 
            
            
            public void processTuple(String fileName, String fileType, boolean isCompressed, byte[] startKey, byte[] endKey) throws IOException {
                last.fileName = fileName;
                last.fileType = fileType;
                last.isCompressed = isCompressed;
                last.startKey = startKey;
                last.endKey = endKey;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentSplit last = new DocumentSplit();
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentSplit clone(DocumentSplit object) {
                DocumentSplit result = new DocumentSplit();
                if (object == null) return result;
                result.fileName = object.fileName; 
                result.fileType = object.fileType; 
                result.isCompressed = object.isCompressed; 
                result.startKey = object.startKey; 
                result.endKey = object.endKey; 
                return result;
            }                 
            
            public void process(DocumentSplit object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                processor.processTuple(object.fileName, object.fileType, object.isCompressed, object.startKey, object.endKey);                                         
            }
                          
            public Class<DocumentSplit> getInputClass() {
                return DocumentSplit.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
    public static class FileNameStartKeyOrder implements Order<DocumentSplit> {
        public int hash(DocumentSplit object) {
            int h = 0;
            h += Utility.hash(object.fileName);
            h += Utility.hash(object.startKey);
            return h;
        } 
        public Comparator<DocumentSplit> greaterThan() {
            return new Comparator<DocumentSplit>() {
                public int compare(DocumentSplit one, DocumentSplit two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fileName, two.fileName);
                        if(result != 0) break;
                        result = + Utility.compare(one.startKey, two.startKey);
                        if(result != 0) break;
                    } while (false);
                    return -result;
                }
            };
        }     
        public Comparator<DocumentSplit> lessThan() {
            return new Comparator<DocumentSplit>() {
                public int compare(DocumentSplit one, DocumentSplit two) {
                    int result = 0;
                    do {
                        result = + Utility.compare(one.fileName, two.fileName);
                        if(result != 0) break;
                        result = + Utility.compare(one.startKey, two.startKey);
                        if(result != 0) break;
                    } while (false);
                    return result;
                }
            };
        }     
        public TypeReader<DocumentSplit> orderedReader(ArrayInput _input) {
            return new ShreddedReader(_input);
        }    

        public TypeReader<DocumentSplit> orderedReader(ArrayInput _input, int bufferSize) {
            return new ShreddedReader(_input, bufferSize);
        }    
        public OrderedWriter<DocumentSplit> orderedWriter(ArrayOutput _output) {
            ShreddedWriter w = new ShreddedWriter(_output);
            return new OrderedWriterClass(w); 
        }                                    
        public static class OrderedWriterClass extends OrderedWriter< DocumentSplit > {
            DocumentSplit last = null;
            ShreddedWriter shreddedWriter = null; 
            
            public OrderedWriterClass(ShreddedWriter s) {
                this.shreddedWriter = s;
            }
            
            public void process(DocumentSplit object) throws IOException {
               boolean processAll = false;
               if (processAll || last == null || 0 != Utility.compare(object.fileName, last.fileName)) { processAll = true; shreddedWriter.processFileName(object.fileName); }
               if (processAll || last == null || 0 != Utility.compare(object.startKey, last.startKey)) { processAll = true; shreddedWriter.processStartKey(object.startKey); }
               shreddedWriter.processTuple(object.fileType, object.isCompressed, object.endKey);
               last = object;
            }           
                 
            public void close() throws IOException {
                shreddedWriter.close();
            }
            
            public Class<DocumentSplit> getInputClass() {
                return DocumentSplit.class;
            }
        } 
        public ReaderSource<DocumentSplit> orderedCombiner(Collection<TypeReader<DocumentSplit>> readers, boolean closeOnExit) {
            ArrayList<ShreddedReader> shreddedReaders = new ArrayList();
            
            for (TypeReader<DocumentSplit> reader : readers) {
                shreddedReaders.add((ShreddedReader)reader);
            }
            
            return new ShreddedCombiner(shreddedReaders, closeOnExit);
        }                  
        public DocumentSplit clone(DocumentSplit object) {
            DocumentSplit result = new DocumentSplit();
            if (object == null) return result;
            result.fileName = object.fileName; 
            result.fileType = object.fileType; 
            result.isCompressed = object.isCompressed; 
            result.startKey = object.startKey; 
            result.endKey = object.endKey; 
            return result;
        }                 
        public Class<DocumentSplit> getOrderedClass() {
            return DocumentSplit.class;
        }                           
        public String[] getOrderSpec() {
            return new String[] {"+fileName", "+startKey"};
        }

        public static String getSpecString() {
            return "+fileName +startKey";
        }
                           
        public interface ShreddedProcessor extends Step {
            public void processFileName(String fileName) throws IOException;
            public void processStartKey(byte[] startKey) throws IOException;
            public void processTuple(String fileType, boolean isCompressed, byte[] endKey) throws IOException;
            public void close() throws IOException;
        }    
        public interface ShreddedSource extends Step {
        }                                              
        
        public static class ShreddedWriter implements ShreddedProcessor {
            ArrayOutput output;
            ShreddedBuffer buffer = new ShreddedBuffer();
            String lastFileName;
            byte[] lastStartKey;
            boolean lastFlush = false;
            
            public ShreddedWriter(ArrayOutput output) {
                this.output = output;
            }                        
            
            public void close() throws IOException {
                flush();
            }
            
            public void processFileName(String fileName) {
                lastFileName = fileName;
                buffer.processFileName(fileName);
            }
            public void processStartKey(byte[] startKey) {
                lastStartKey = startKey;
                buffer.processStartKey(startKey);
            }
            public final void processTuple(String fileType, boolean isCompressed, byte[] endKey) throws IOException {
                if (lastFlush) {
                    if(buffer.fileNames.size() == 0) buffer.processFileName(lastFileName);
                    if(buffer.startKeys.size() == 0) buffer.processStartKey(lastStartKey);
                    lastFlush = false;
                }
                buffer.processTuple(fileType, isCompressed, endKey);
                if (buffer.isFull())
                    flush();
            }
            public final void flushTuples(int pauseIndex) throws IOException {
                
                while (buffer.getReadIndex() < pauseIndex) {
                           
                    output.writeString(buffer.getFileType());
                    output.writeBoolean(buffer.getIsCompressed());
                    output.writeBytes(buffer.getEndKey());
                    buffer.incrementTuple();
                }
            }  
            public final void flushFileName(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getFileNameEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeString(buffer.getFileName());
                    output.writeInt(count);
                    buffer.incrementFileName();
                      
                    flushStartKey(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public final void flushStartKey(int pauseIndex) throws IOException {
                while (buffer.getReadIndex() < pauseIndex) {
                    int nextPause = buffer.getStartKeyEndIndex();
                    int count = nextPause - buffer.getReadIndex();
                    
                    output.writeBytes(buffer.getStartKey());
                    output.writeInt(count);
                    buffer.incrementStartKey();
                      
                    flushTuples(nextPause);
                    assert nextPause == buffer.getReadIndex();
                }
            }
            public void flush() throws IOException { 
                flushFileName(buffer.getWriteIndex());
                buffer.reset(); 
                lastFlush = true;
            }                           
        }
        public static class ShreddedBuffer {
            ArrayList<String> fileNames = new ArrayList();
            ArrayList<byte[]> startKeys = new ArrayList();
            ArrayList<Integer> fileNameTupleIdx = new ArrayList();
            ArrayList<Integer> startKeyTupleIdx = new ArrayList();
            int fileNameReadIdx = 0;
            int startKeyReadIdx = 0;
                            
            String[] fileTypes;
            boolean[] isCompresseds;
            byte[][] endKeys;
            int writeTupleIndex = 0;
            int readTupleIndex = 0;
            int batchSize;

            public ShreddedBuffer(int batchSize) {
                this.batchSize = batchSize;

                fileTypes = new String[batchSize];
                isCompresseds = new boolean[batchSize];
                endKeys = new byte[batchSize][];
            }                              

            public ShreddedBuffer() {    
                this(10000);
            }                                                                                                                    
            
            public void processFileName(String fileName) {
                fileNames.add(fileName);
                fileNameTupleIdx.add(writeTupleIndex);
            }                                      
            public void processStartKey(byte[] startKey) {
                startKeys.add(startKey);
                startKeyTupleIdx.add(writeTupleIndex);
            }                                      
            public void processTuple(String fileType, boolean isCompressed, byte[] endKey) {
                assert fileNames.size() > 0;
                assert startKeys.size() > 0;
                fileTypes[writeTupleIndex] = fileType;
                isCompresseds[writeTupleIndex] = isCompressed;
                endKeys[writeTupleIndex] = endKey;
                writeTupleIndex++;
            }
            public void resetData() {
                fileNames.clear();
                startKeys.clear();
                fileNameTupleIdx.clear();
                startKeyTupleIdx.clear();
                writeTupleIndex = 0;
            }                  
                                 
            public void resetRead() {
                readTupleIndex = 0;
                fileNameReadIdx = 0;
                startKeyReadIdx = 0;
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
            public void incrementFileName() {
                fileNameReadIdx++;  
            }                                                                                              

            public void autoIncrementFileName() {
                while (readTupleIndex >= getFileNameEndIndex() && readTupleIndex < writeTupleIndex)
                    fileNameReadIdx++;
            }                 
            public void incrementStartKey() {
                startKeyReadIdx++;  
            }                                                                                              

            public void autoIncrementStartKey() {
                while (readTupleIndex >= getStartKeyEndIndex() && readTupleIndex < writeTupleIndex)
                    startKeyReadIdx++;
            }                 
            public void incrementTuple() {
                readTupleIndex++;
            }                    
            public int getFileNameEndIndex() {
                if ((fileNameReadIdx+1) >= fileNameTupleIdx.size())
                    return writeTupleIndex;
                return fileNameTupleIdx.get(fileNameReadIdx+1);
            }

            public int getStartKeyEndIndex() {
                if ((startKeyReadIdx+1) >= startKeyTupleIdx.size())
                    return writeTupleIndex;
                return startKeyTupleIdx.get(startKeyReadIdx+1);
            }
            public int getReadIndex() {
                return readTupleIndex;
            }   

            public int getWriteIndex() {
                return writeTupleIndex;
            } 
            public String getFileName() {
                assert readTupleIndex < writeTupleIndex;
                assert fileNameReadIdx < fileNames.size();
                
                return fileNames.get(fileNameReadIdx);
            }
            public byte[] getStartKey() {
                assert readTupleIndex < writeTupleIndex;
                assert startKeyReadIdx < startKeys.size();
                
                return startKeys.get(startKeyReadIdx);
            }
            public String getFileType() {
                assert readTupleIndex < writeTupleIndex;
                return fileTypes[readTupleIndex];
            }                                         
            public boolean getIsCompressed() {
                assert readTupleIndex < writeTupleIndex;
                return isCompresseds[readTupleIndex];
            }                                         
            public byte[] getEndKey() {
                assert readTupleIndex < writeTupleIndex;
                return endKeys[readTupleIndex];
            }                                         
            public void copyTuples(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                   output.processTuple(getFileType(), getIsCompressed(), getEndKey());
                   incrementTuple();
                }
            }                                                                           
            public void copyUntilIndexFileName(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processFileName(getFileName());
                    assert getFileNameEndIndex() <= endIndex;
                    copyUntilIndexStartKey(getFileNameEndIndex(), output);
                    incrementFileName();
                }
            } 
            public void copyUntilIndexStartKey(int endIndex, ShreddedProcessor output) throws IOException {
                while (getReadIndex() < endIndex) {
                    output.processStartKey(getStartKey());
                    assert getStartKeyEndIndex() <= endIndex;
                    copyTuples(getStartKeyEndIndex(), output);
                    incrementStartKey();
                }
            }  
            public void copyUntilFileName(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getFileName(), other.getFileName());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processFileName(getFileName());
                                      
                        if (c < 0) {
                            copyUntilIndexStartKey(getFileNameEndIndex(), output);
                        } else if (c == 0) {
                            copyUntilStartKey(other, output);
                            autoIncrementFileName();
                            break;
                        }
                    } else {
                        output.processFileName(getFileName());
                        copyUntilIndexStartKey(getFileNameEndIndex(), output);
                    }
                    incrementFileName();  
                    
               
                }
            }
            public void copyUntilStartKey(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                while (!isAtEnd()) {
                    if (other != null) {   
                        assert !other.isAtEnd();
                        int c = + Utility.compare(getStartKey(), other.getStartKey());
                    
                        if (c > 0) {
                            break;   
                        }
                        
                        output.processStartKey(getStartKey());
                                      
                        copyTuples(getStartKeyEndIndex(), output);
                    } else {
                        output.processStartKey(getStartKey());
                        copyTuples(getStartKeyEndIndex(), output);
                    }
                    incrementStartKey();  
                    
                    if (getFileNameEndIndex() <= readTupleIndex)
                        break;   
                }
            }
            public void copyUntil(ShreddedBuffer other, ShreddedProcessor output) throws IOException {
                copyUntilFileName(other, output);
            }
            
        }                         
        public static class ShreddedCombiner implements ReaderSource<DocumentSplit>, ShreddedSource {   
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
                } else if (processor instanceof DocumentSplit.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentSplit.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<DocumentSplit>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentSplit> getOutputClass() {
                return DocumentSplit.class;
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

            public DocumentSplit read() throws IOException {
                if (uninitialized)
                    initialize();

                DocumentSplit result = null;

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
        public static class ShreddedReader implements Step, Comparable<ShreddedReader>, TypeReader<DocumentSplit>, ShreddedSource {      
            public ShreddedProcessor processor;
            ShreddedBuffer buffer;
            DocumentSplit last = new DocumentSplit();         
            long updateFileNameCount = -1;
            long updateStartKeyCount = -1;
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
                    result = + Utility.compare(buffer.getFileName(), otherBuffer.getFileName());
                    if(result != 0) break;
                    result = + Utility.compare(buffer.getStartKey(), otherBuffer.getStartKey());
                    if(result != 0) break;
                } while (false);                                             
                
                return result;
            }
            
            public final ShreddedBuffer getBuffer() {
                return buffer;
            }                
            
            public final DocumentSplit read() throws IOException {
                if (buffer.isAtEnd()) {
                    fill();             
                
                    if (buffer.isAtEnd()) {
                        return null;
                    }
                }
                      
                assert !buffer.isAtEnd();
                DocumentSplit result = new DocumentSplit();
                
                result.fileName = buffer.getFileName();
                result.startKey = buffer.getStartKey();
                result.fileType = buffer.getFileType();
                result.isCompressed = buffer.getIsCompressed();
                result.endKey = buffer.getEndKey();
                
                buffer.incrementTuple();
                buffer.autoIncrementFileName();
                buffer.autoIncrementStartKey();
                
                return result;
            }           
            
            public final void fill() throws IOException {
                try {   
                    buffer.reset();
                    
                    if (tupleCount != 0) {
                                                      
                        if(updateFileNameCount - tupleCount > 0) {
                            buffer.fileNames.add(last.fileName);
                            buffer.fileNameTupleIdx.add((int) (updateFileNameCount - tupleCount));
                        }                              
                        if(updateStartKeyCount - tupleCount > 0) {
                            buffer.startKeys.add(last.startKey);
                            buffer.startKeyTupleIdx.add((int) (updateStartKeyCount - tupleCount));
                        }
                        bufferStartCount = tupleCount;
                    }
                    
                    while (!buffer.isFull()) {
                        updateStartKey();
                        buffer.processTuple(input.readString(), input.readBoolean(), input.readBytes());
                        tupleCount++;
                    }
                } catch(EOFException e) {}
            }

            public final void updateFileName() throws IOException {
                if (updateFileNameCount > tupleCount)
                    return;
                     
                last.fileName = input.readString();
                updateFileNameCount = tupleCount + input.readInt();
                                      
                buffer.processFileName(last.fileName);
            }
            public final void updateStartKey() throws IOException {
                if (updateStartKeyCount > tupleCount)
                    return;
                     
                updateFileName();
                last.startKey = input.readBytes();
                updateStartKeyCount = tupleCount + input.readInt();
                                      
                buffer.processStartKey(last.startKey);
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
                } else if (processor instanceof DocumentSplit.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((DocumentSplit.Processor) processor));
                } else if (processor instanceof org.galagosearch.tupleflow.Processor) {
                    this.processor = new DuplicateEliminator(new TupleUnshredder((org.galagosearch.tupleflow.Processor<DocumentSplit>) processor));
                } else {
                    throw new IncompatibleProcessorException(processor.getClass().getName() + " is not supported by " + this.getClass().getName());                                                                       
                }
            }                                
            
            public Class<DocumentSplit> getOutputClass() {
                return DocumentSplit.class;
            }                
        }
        
        public static class DuplicateEliminator implements ShreddedProcessor {
            public ShreddedProcessor processor;
            DocumentSplit last = new DocumentSplit();
            boolean fileNameProcess = true;
            boolean startKeyProcess = true;
                                           
            public DuplicateEliminator() {}
            public DuplicateEliminator(ShreddedProcessor processor) {
                this.processor = processor;
            }
            
            public void setShreddedProcessor(ShreddedProcessor processor) {
                this.processor = processor;
            }

            public void processFileName(String fileName) throws IOException {  
                if (fileNameProcess || Utility.compare(fileName, last.fileName) != 0) {
                    last.fileName = fileName;
                    processor.processFileName(fileName);
            resetStartKey();
                    fileNameProcess = false;
                }
            }
            public void processStartKey(byte[] startKey) throws IOException {  
                if (startKeyProcess || Utility.compare(startKey, last.startKey) != 0) {
                    last.startKey = startKey;
                    processor.processStartKey(startKey);
                    startKeyProcess = false;
                }
            }  
            
            public void resetFileName() {
                 fileNameProcess = true;
            resetStartKey();
            }                                                
            public void resetStartKey() {
                 startKeyProcess = true;
            }                                                
                               
            public void processTuple(String fileType, boolean isCompressed, byte[] endKey) throws IOException {
                processor.processTuple(fileType, isCompressed, endKey);
            } 
            
            public void close() throws IOException {
                processor.close();
            }                    
        }
        public static class TupleUnshredder implements ShreddedProcessor {
            DocumentSplit last = new DocumentSplit();
            public org.galagosearch.tupleflow.Processor<DocumentSplit> processor;                               
            
            public TupleUnshredder(DocumentSplit.Processor processor) {
                this.processor = processor;
            }         
            
            public TupleUnshredder(org.galagosearch.tupleflow.Processor<DocumentSplit> processor) {
                this.processor = processor;
            }
            
            public DocumentSplit clone(DocumentSplit object) {
                DocumentSplit result = new DocumentSplit();
                if (object == null) return result;
                result.fileName = object.fileName; 
                result.fileType = object.fileType; 
                result.isCompressed = object.isCompressed; 
                result.startKey = object.startKey; 
                result.endKey = object.endKey; 
                return result;
            }                 
            
            public void processFileName(String fileName) throws IOException {
                last.fileName = fileName;
            }   
                
            public void processStartKey(byte[] startKey) throws IOException {
                last.startKey = startKey;
            }   
                
            
            public void processTuple(String fileType, boolean isCompressed, byte[] endKey) throws IOException {
                last.fileType = fileType;
                last.isCompressed = isCompressed;
                last.endKey = endKey;
                processor.process(clone(last));
            }               
            
            public void close() throws IOException {
                processor.close();
            }
        }     
        public static class TupleShredder implements Processor {
            DocumentSplit last = new DocumentSplit();
            public ShreddedProcessor processor;
            
            public TupleShredder(ShreddedProcessor processor) {
                this.processor = processor;
            }                              
            
            public DocumentSplit clone(DocumentSplit object) {
                DocumentSplit result = new DocumentSplit();
                if (object == null) return result;
                result.fileName = object.fileName; 
                result.fileType = object.fileType; 
                result.isCompressed = object.isCompressed; 
                result.startKey = object.startKey; 
                result.endKey = object.endKey; 
                return result;
            }                 
            
            public void process(DocumentSplit object) throws IOException {                                                                                                                                                   
                boolean processAll = false;
                if(last == null || Utility.compare(last.fileName, object.fileName) != 0 || processAll) { processor.processFileName(object.fileName); processAll = true; }
                if(last == null || Utility.compare(last.startKey, object.startKey) != 0 || processAll) { processor.processStartKey(object.startKey); processAll = true; }
                processor.processTuple(object.fileType, object.isCompressed, object.endKey);                                         
            }
                          
            public Class<DocumentSplit> getInputClass() {
                return DocumentSplit.class;
            }
            
            public void close() throws IOException {
                processor.close();
            }                     
        }
    } 
}    