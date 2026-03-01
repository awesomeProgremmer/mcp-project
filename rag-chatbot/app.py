import streamlit as st
from langchain_huggingface import HuggingFaceEmbeddings, HuggingFacePipeline
from langchain_community.vectorstores import FAISS
from langchain_core.prompts import PromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from transformers import pipeline
import torch

# ── Page config ──────────────────────────────────────────────
st.set_page_config(page_title="RAG Chatbot", layout="centered")
st.title("🤖 RAG Chatbot")
st.caption("Ask anything from your PDF document")

# ── Load vectorstore & LLM (cached so it only loads once) ────
@st.cache_resource
def load_vectorstore():
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    vectorstore = FAISS.load_local(
        "faiss_index", embeddings, allow_dangerous_deserialization=True
    )
    return vectorstore

@st.cache_resource
def load_llm():
    pipe = pipeline(
        "text-generation",
        model="TinyLlama/TinyLlama-1.1B-Chat-v1.0",
        max_new_tokens=256,
        do_sample=False,
        device=0 if torch.cuda.is_available() else -1,
    )
    return HuggingFacePipeline(pipeline=pipe)

# ── Load resources ───────────────────────────────────────────
vectorstore = load_vectorstore()
llm = load_llm()

retriever = vectorstore.as_retriever(search_kwargs={"k": 3})

# ── Build modern LCEL chain ───────────────────────────────────
prompt = PromptTemplate.from_template(
    "Use the context below to answer the question.\n\n"
    "Context: {context}\n\n"
    "Question: {question}\n\n"
    "Answer:"
)

def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)

qa_chain = (
    {"context": retriever | format_docs, "question": RunnablePassthrough()}
    | prompt
    | llm
    | StrOutputParser()
)

# ── Chat history ─────────────────────────────────────────────
if "messages" not in st.session_state:
    st.session_state.messages = []

for msg in st.session_state.messages:
    with st.chat_message(msg["role"]):
        st.markdown(msg["content"])

# ── Chat input ───────────────────────────────────────────────
if user_input := st.chat_input("Ask a question about your PDF..."):

    st.session_state.messages.append({"role": "user", "content": user_input})
    with st.chat_message("user"):
        st.markdown(user_input)

    with st.chat_message("assistant"):
        with st.spinner("Thinking..."):
            answer = qa_chain.invoke(user_input)

        st.markdown(answer)

        with st.expander("Source chunks used"):
            docs = retriever.invoke(user_input)
            for i, doc in enumerate(docs):
                st.markdown(f"**Chunk {i+1}:**")
                st.write(doc.page_content)
                st.divider()

    st.session_state.messages.append({"role": "assistant", "content": answer})
