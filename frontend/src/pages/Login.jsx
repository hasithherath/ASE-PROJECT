import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/api';
import { Library, Lock, User } from 'lucide-react';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await authApi.login(username, password);
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('userId', response.data.id);
            localStorage.setItem('username', response.data.username);
            localStorage.setItem('roles', JSON.stringify(response.data.roles));
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid credentials');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[80vh] px-4">
            <div className="bg-white p-8 rounded-3xl shadow-2xl shadow-blue-900/5 w-full max-w-md border border-slate-100">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center p-4 bg-blue-50 rounded-2xl text-blue-600 mb-4 shadow-inner">
                        <Library size={40} />
                    </div>
                    <h1 className="text-3xl font-black text-slate-900 tracking-tight">Welcome Back</h1>
                    <p className="text-slate-500 mt-2 font-medium">Digital Library Management System</p>
                </div>
                
                {error && (
                    <div className="bg-red-50 border border-red-100 text-red-600 p-4 rounded-2xl text-sm mb-6 text-center font-semibold animate-shake">
                        {error}
                    </div>
                )}
                
                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Username</label>
                        <div className="relative group">
                            <User className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors" size={20} />
                            <input
                                type="text"
                                className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 pl-12 pr-4 text-slate-900 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all font-medium"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Enter username"
                                required
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Password</label>
                        <div className="relative group">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors" size={20} />
                            <input
                                type="password"
                                className="w-full bg-slate-50 border border-slate-200 rounded-2xl py-4 pl-12 pr-4 text-slate-900 outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all font-medium"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                            />
                        </div>
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-blue-600/25 active:scale-95 text-lg"
                    >
                        Sign In
                    </button>
                </form>

                <div className="mt-10 pt-6 border-t border-slate-100">
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em] text-center mb-5">Quick Access Accounts</p>
                    <div className="grid grid-cols-2 gap-4">
                        <button 
                            onClick={() => { setUsername('librarian'); setPassword('password123'); }}
                            className="bg-slate-50 hover:bg-white hover:shadow-md border border-slate-200 p-4 rounded-2xl text-left transition-all group active:scale-95"
                        >
                            <p className="text-xs text-blue-600 font-black mb-1">LIBRARIAN</p>
                            <p className="text-[11px] text-slate-500 font-medium">Admin Mode</p>
                        </button>
                        <button 
                            onClick={() => { setUsername('member1'); setPassword('password123'); }}
                            className="bg-slate-50 hover:bg-white hover:shadow-md border border-slate-200 p-4 rounded-2xl text-left transition-all group active:scale-95"
                        >
                            <p className="text-xs text-green-600 font-black mb-1">MEMBER</p>
                            <p className="text-[11px] text-slate-500 font-medium">Student Mode</p>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
