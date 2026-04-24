import React, { useEffect, useState } from 'react';
import { bookApi } from '../api/api';
import { Plus, Edit2, Trash2, X, Save, Search, Settings2, Package } from 'lucide-react';

const LibrarianView = () => {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingBook, setEditingBook] = useState(null);
    const [formData, setFormData] = useState({
        title: '',
        author: '',
        isbn: '',
        category: 'Classic',
        availableCopies: 5
    });

    useEffect(() => {
        fetchBooks();
    }, []);

    const fetchBooks = async () => {
        try {
            const res = await bookApi.getAll({ size: 100 });
            setBooks(res.data.content);
        } catch (err) {
            console.error('Failed to fetch books', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingBook) {
                await bookApi.update(editingBook.id, formData);
            } else {
                await bookApi.add(formData);
            }
            setShowModal(false);
            setEditingBook(null);
            setFormData({ title: '', author: '', isbn: '', category: 'Classic', availableCopies: 5 });
            fetchBooks();
        } catch (err) {
            alert('Operation failed. Please check your data.');
        }
    };

    const handleEdit = (book) => {
        setEditingBook(book);
        setFormData({
            title: book.title,
            author: book.author,
            isbn: book.isbn,
            category: book.category,
            availableCopies: book.availableCopies
        });
        setShowModal(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this book?')) {
            try {
                await bookApi.delete(id);
                fetchBooks();
            } catch (err) {
                alert('Failed to delete book');
            }
        }
    };

    return (
        <div className="space-y-8 animate-fade-in">
            <header className="flex flex-col md:flex-row md:items-center justify-between gap-6 bg-white p-8 rounded-3xl border border-slate-100 shadow-sm">
                <div>
                    <div className="flex items-center space-x-2 text-blue-600 mb-1">
                        <Settings2 size={16} className="animate-spin-slow" />
                        <span className="text-[10px] font-black uppercase tracking-[0.2em]">Inventory Management</span>
                    </div>
                    <h1 className="text-3xl font-black text-slate-900 tracking-tight">Library Assets</h1>
                    <p className="text-slate-500 font-medium mt-1">Librarian Management Interface</p>
                </div>
                
                <button
                    onClick={() => { setEditingBook(null); setShowModal(true); }}
                    className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white font-black px-8 py-4 rounded-2xl transition-all shadow-lg shadow-blue-600/25 active:scale-95"
                >
                    <Plus size={20} />
                    <span>Deploy New Asset</span>
                </button>
            </header>

            <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50/50 text-slate-400 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-8 py-5">Asset Information</th>
                                <th className="px-8 py-5">Classification</th>
                                <th className="px-8 py-5">Stock Level</th>
                                <th className="px-8 py-5 text-right">Operations</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                            {loading ? (
                                <tr><td colSpan="4" className="px-8 py-10 text-center">Loading assets...</td></tr>
                            ) : (
                                books.map(book => (
                                    <tr key={book.id} className="hover:bg-slate-50/50 transition-colors group">
                                        <td className="px-8 py-6">
                                            <div>
                                                <p className="font-bold text-slate-900 uppercase text-sm tracking-tight">{book.title}</p>
                                                <p className="text-xs text-slate-400 font-medium">ISBN: {book.isbn}</p>
                                            </div>
                                        </td>
                                        <td className="px-8 py-6">
                                            <span className="px-3 py-1 bg-slate-100 text-slate-600 rounded-full text-[10px] font-black uppercase tracking-widest border border-slate-200">
                                                {book.category}
                                            </span>
                                        </td>
                                        <td className="px-8 py-6">
                                            <div className="flex items-center space-x-2">
                                                <span className={`text-sm font-black ${book.availableCopies < 2 ? 'text-rose-600' : 'text-slate-900'}`}>
                                                    {book.availableCopies}
                                                </span>
                                                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">Units</span>
                                            </div>
                                        </td>
                                        <td className="px-8 py-6">
                                            <div className="flex items-center justify-end space-x-3">
                                                <button 
                                                    onClick={() => handleEdit(book)}
                                                    className="p-2 text-blue-600 hover:bg-blue-50 rounded-xl transition-colors border border-transparent hover:border-blue-100"
                                                >
                                                    <Edit2 size={18} />
                                                </button>
                                                <button 
                                                    onClick={() => handleDelete(book.id)}
                                                    className="p-2 text-rose-600 hover:bg-rose-50 rounded-xl transition-colors border border-transparent hover:border-rose-100"
                                                >
                                                    <Trash2 size={18} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {showModal && (
                <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fade-in">
                    <div className="bg-white rounded-3xl shadow-2xl w-full max-w-xl border border-slate-100 overflow-hidden animate-slide-up">
                        <div className="p-8 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
                            <div className="flex items-center space-x-3 text-blue-600">
                                <Package size={24} />
                                <h2 className="text-2xl font-black text-slate-900 tracking-tight">
                                    {editingBook ? 'Edit Asset Data' : 'Configure New Asset'}
                                </h2>
                            </div>
                            <button onClick={() => setShowModal(false)} className="p-2 text-slate-400 hover:bg-slate-200 rounded-xl transition-colors">
                                <X size={24} />
                            </button>
                        </div>
                        
                        <form onSubmit={handleSubmit} className="p-8 space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="md:col-span-2">
                                    <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Asset Title</label>
                                    <input
                                        type="text"
                                        required
                                        className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 px-5 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 font-bold text-slate-700 transition-all placeholder:text-slate-300"
                                        placeholder="Enter full book title"
                                        value={formData.title}
                                        onChange={(e) => setFormData({...formData, title: e.target.value})}
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Author Name</label>
                                    <input
                                        type="text"
                                        required
                                        className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 px-5 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 font-bold text-slate-700 transition-all placeholder:text-slate-300"
                                        placeholder="Reference name"
                                        value={formData.author}
                                        onChange={(e) => setFormData({...formData, author: e.target.value})}
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Serial Number (ISBN)</label>
                                    <input
                                        type="text"
                                        required
                                        className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 px-5 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 font-bold text-slate-700 transition-all placeholder:text-slate-300"
                                        placeholder="Unique ID"
                                        value={formData.isbn}
                                        onChange={(e) => setFormData({...formData, isbn: e.target.value})}
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Classification</label>
                                    <select
                                        className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 px-5 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 font-bold text-slate-700 transition-all appearance-none"
                                        value={formData.category}
                                        onChange={(e) => setFormData({...formData, category: e.target.value})}
                                    >
                                        <option value="Classic">Classic</option>
                                        <option value="Dystopian">Dystopian</option>
                                        <option value="Fantasy">Fantasy</option>
                                        <option value="Satire">Satire</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Stock Amount</label>
                                    <input
                                        type="number"
                                        required
                                        min="0"
                                        className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 px-5 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 font-bold text-slate-700 transition-all placeholder:text-slate-300"
                                        value={formData.availableCopies}
                                        onChange={(e) => setFormData({...formData, availableCopies: parseInt(e.target.value)})}
                                    />
                                </div>
                            </div>
                            
                            <div className="flex gap-4 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-600 font-black py-4 rounded-2xl transition-all active:scale-95"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-2xl transition-all shadow-lg shadow-blue-600/20 active:scale-95 flex items-center justify-center space-x-2 uppercase tracking-wide text-xs"
                                >
                                    <Save size={18} />
                                    <span>{editingBook ? 'Authorize Sync' : 'Commit Changes'}</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LibrarianView;
