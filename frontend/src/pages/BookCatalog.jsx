import React, { useEffect, useState } from 'react';
import { bookApi, borrowApi, memberApi } from '../api/api';
import { Search, Filter, Book, ChevronLeft, ChevronRight, CheckCircle, AlertCircle } from 'lucide-react';

const BookCatalog = () => {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [category, setCategory] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [member, setMember] = useState(null);
    const [message, setMessage] = useState(null);

    const categories = ['All', 'Classic', 'Dystopian', 'Fantasy', 'Satire', 'Fiction', 'Non-Fiction'];

    useEffect(() => {
        fetchBooks();
        fetchMember();
    }, [page, search, category]);

    const fetchBooks = async () => {
        try {
            const res = await bookApi.getAll({
                page,
                size: 6,
                title: search,
                category: category === 'All' ? '' : category
            });
            setBooks(res.data.content);
            setTotalPages(res.data.totalPages);
        } catch (err) {
            console.error('Failed to fetch books', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchMember = async () => {
        try {
            const res = await memberApi.getMe();
            setMember(res.data);
        } catch (err) {
            console.error('Failed to fetch member', err);
        }
    };

    const handleBorrow = async (bookId) => {
        if (!member) return;
        try {
            await borrowApi.borrow(bookId, member.id);
            setMessage({ type: 'success', text: 'Book borrowed successfully!' });
            fetchBooks();
        } catch (err) {
            setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to borrow book' });
        }
        setTimeout(() => setMessage(null), 3000);
    };

    return (
        <div className="space-y-8 animate-fade-in">
            <header className="flex flex-col md:flex-row md:items-center justify-between gap-6 bg-white p-8 rounded-3xl border border-slate-100 shadow-sm">
                <div>
                    <h1 className="text-3xl font-black text-slate-900 tracking-tight">Discovery Catalog</h1>
                    <p className="text-slate-500 font-medium mt-1">Explore our collection of over 5,000 titles</p>
                </div>
                
                <div className="flex flex-col sm:flex-row gap-4 flex-1 max-w-2xl">
                    <div className="relative flex-1 group">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors" size={18} />
                        <input
                            type="text"
                            placeholder="Search by title..."
                            className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-3 pl-12 pr-4 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all font-semibold text-slate-700"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                    <div className="relative group">
                        <Filter className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors" size={18} />
                        <select
                            className="bg-slate-50 border border-slate-200 rounded-2xl py-3 pl-12 pr-10 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all font-semibold appearance-none text-slate-700"
                            value={category}
                            onChange={(e) => setCategory(e.target.value)}
                        >
                            {categories.map(c => (
                                <option key={c} value={c}>{c}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </header>

            {message && (
                <div className={`p-4 rounded-2xl flex items-center shadow-lg border animate-bounce ${
                    message.type === 'success' ? 'bg-green-50 border-green-100 text-green-700' : 'bg-red-50 border-red-100 text-red-700'
                }`}>
                    {message.type === 'success' ? <CheckCircle className="mr-3" size={20} /> : <AlertCircle className="mr-3" size={20} />}
                    <span className="font-bold">{message.text}</span>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {loading ? (
                    [...Array(6)].map((_, i) => (
                        <div key={i} className="bg-white p-6 rounded-3xl border border-slate-100 h-64 animate-pulse shadow-sm">
                            <div className="w-1/4 h-8 bg-slate-100 rounded-xl mb-4" />
                            <div className="w-3/4 h-6 bg-slate-100 rounded-lg mb-2" />
                            <div className="w-1/2 h-6 bg-slate-100 rounded-lg mb-6" />
                            <div className="w-full h-12 bg-slate-100 rounded-xl" />
                        </div>
                    ))
                ) : (
                    books.map(book => (
                        <div key={book.id} className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm hover:shadow-xl transition-all duration-300 group overflow-hidden relative">
                            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                                <Book size={100} />
                            </div>
                            
                            <div className="mb-6">
                                <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-[10px] font-black uppercase tracking-widest border border-blue-100">
                                    {book.category}
                                </span>
                            </div>
                            
                            <div className="min-h-[100px]">
                                <h3 className="text-xl font-black text-slate-900 leading-tight mb-2 uppercase tracking-tight line-clamp-2">
                                    {book.title}
                                </h3>
                                <p className="text-slate-500 font-semibold mb-4 text-sm">by {book.author}</p>
                                <p className="text-xs text-slate-400 font-bold tracking-tighter mb-6">ISBN: {book.isbn}</p>
                            </div>

                            <div className="flex items-center justify-between mt-auto pt-6 border-t border-slate-50">
                                <div className="flex flex-col">
                                    <span className={`text-xl font-black ${book.availableCopies > 0 ? 'text-slate-900' : 'text-rose-600'}`}>
                                        {book.availableCopies}
                                    </span>
                                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Available</span>
                                </div>
                                
                                <button
                                    onClick={() => handleBorrow(book.id)}
                                    disabled={book.availableCopies === 0}
                                    className={`px-6 py-3 rounded-2xl font-black text-sm transition-all duration-200 active:scale-95 flex items-center space-x-2 ${
                                        book.availableCopies > 0 
                                            ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/20 hover:bg-blue-700' 
                                            : 'bg-slate-100 text-slate-400 cursor-not-allowed'
                                    }`}
                                >
                                    <span>{book.availableCopies > 0 ? 'Borrow Now' : 'Out of Stock'}</span>
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>

            <footer className="flex items-center justify-center space-x-6 pt-10 pb-4">
                <button
                    disabled={page === 0}
                    onClick={() => setPage(p => p - 1)}
                    className="p-3 bg-white rounded-2xl border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition-all shadow-sm active:scale-90"
                >
                    <ChevronLeft size={20} />
                </button>
                <div className="flex items-center space-x-2">
                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em]">Page</span>
                    <span className="w-10 h-10 bg-blue-600 text-white rounded-xl flex items-center justify-center font-black shadow-lg shadow-blue-600/20">{page + 1}</span>
                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em]">of {totalPages}</span>
                </div>
                <button
                    disabled={page === totalPages - 1}
                    onClick={() => setPage(p => p + 1)}
                    className="p-3 bg-white rounded-2xl border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition-all shadow-sm active:scale-90"
                >
                    <ChevronRight size={20} />
                </button>
            </footer>
        </div>
    );
};

export default BookCatalog;
