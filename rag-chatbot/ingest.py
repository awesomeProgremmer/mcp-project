from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings

# Step 1: Load PDF
loader = PyPDFLoader("data/sample.pdf")
documents = loader.load()
print(f"Total pages loaded: {len(documents)}")

# Step 2: Chunk the documents
splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
chunks = splitter.split_documents(documents)
print(f"Total chunks created: {len(chunks)}")

# Step 3: Create embeddings using HuggingFace (free, runs locally)
print("Loading embedding model... (first time may take a minute)")
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

# Step 4: Store embeddings in FAISS vector database
vectorstore = FAISS.from_documents(chunks, embeddings)
vectorstore.save_local("faiss_index")

print("✅ Done! Vector store saved to faiss_index/")