import React, { useEffect, useState } from 'react';
import { memberApi } from '../api/api';
import { Book, CreditCard, History, Clock, Target } from 'lucide-react';

const Dashboard = () => {
    const [member, setMember] = useState(null);
    const [history, setHistory] = useState([]);
    const [totalFines, setTotalFines] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const meRes = await memberApi.getMe();
            const memberId = meRes.data.id;
            setMember(meRes.data);

            const [historyRes, finesRes] = await Promise.all([
                memberApi.getHistory(memberId),
                memberApi.getFines(memberId)
            ]);

            setHistory(historyRes.data);
            setTotalFines(finesRes.data.totalFines);
        } catch (err) {
            console.error('Failed to fetch dashboard data', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return (
        <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
    );

    const activeBorrows = history.filter(t => !t.returnDate);

    const StatCard = ({ icon: Icon, label, value, subtext, colorClass, progress }) => (
        <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-4">
                <div className={`p-3 rounded-2xl ${colorClass}`}>
                    <Icon size={24} />
                </div>
                <span className="text-[10px] font-black uppercase tracking-widest text-slate-400">{label}</span>
            </div>
            <div className="text-3xl font-black text-slate-900">{value}</div>
            <p className="text-sm font-medium text-slate-500 mt-1">{subtext}</p>
            {progress !== undefined && (
                <div className="mt-4 bg-slate-100 h-2rounded-full overflow-hidden">
                    <div 
                        className={`h-full transition-all duration-1000 rounded-full ${colorClass.replace('bg-', 'bg-').replace('/10', '')}`} 
                        style={{ width: `${Math.min(progress, 100)}%` }}
                    />
                </div>
            )}
        </div>
    );

    return (
        <div className="space-y-10 animate-fade-in">
            <header className="flex justify-between items-end">
                <div>
                    <h1 className="text-4xl font-black text-slate-900 tracking-tight">System Overview</h1>
                    <p className="text-slate-500 mt-2 font-semibold">Welcome back, <span className="text-blue-600">{member?.firstName}</span> 👋</p>
                </div>
                <div className="hidden md:block text-right">
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-widest leading-relaxed">Member Tier</p>
                    <p className="text-sm font-black text-slate-900">Standard Access</p>
                </div>
            </header>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <StatCard 
                    icon={Book} 
                    label="Books Issued" 
                    value={activeBorrows.length} 
                    subtext="Out of 5 book limit"
                    colorClass="bg-blue-50 text-blue-600"
                    progress={(activeBorrows.length / 5) * 100}
                />
                <StatCard 
                    icon={CreditCard} 
                    label="Active Fines" 
                    value={`$${totalFines.toFixed(2)}`} 
                    subtext="Please settle at front desk"
                    colorClass="bg-rose-50 text-rose-600"
                />
                <StatCard 
                    icon={History} 
                    label="Loan History" 
                    value={history.length} 
                    subtext="Total lifetime rentals"
                    colorClass="bg-amber-50 text-amber-600"
                />
            </div>

            <section className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
                <div className="p-8 border-b border-slate-50 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                        <div className="p-2 bg-blue-50 rounded-xl text-blue-600">
                            <Clock size={20} />
                        </div>
                        <h2 className="text-xl font-black text-slate-900 tracking-tight">Active Borrowed Books</h2>
                    </div>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50/50 text-slate-400 text-[10px] font-black uppercase tracking-widest">
                            <tr>
                                <th className="px-8 py-5">Book Identification</th>
                                <th className="px-8 py-5">Issue Date</th>
                                <th className="px-8 py-5">Deadline</th>
                                <th className="px-8 py-5 text-right">Action status</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                            {activeBorrows.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="px-8 py-16 text-center text-slate-400 font-medium">
                                        <div className="flex flex-col items-center">
                                            <Target size={40} className="mb-4 opacity-20" />
                                            <p>Your shelf is currently empty.</p>
                                        </div>
                                    </td>
                                </tr>
                            ) : (
                                activeBorrows.map(t => (
                                    <tr key={t.id} className="hover:bg-slate-50/50 transition-colors group">
                                        <td className="px-8 py-6">
                                            <div>
                                                <p className="font-bold text-slate-900 group-hover:text-blue-600 transition-colors uppercase text-sm tracking-tight">{t.book.title}</p>
                                                <p className="text-xs text-slate-400 font-medium">{t.book.author}</p>
                                            </div>
                                        </td>
                                        <td className="px-8 py-6 text-sm font-semibold text-slate-600">{t.borrowDate}</td>
                                        <td className="px-8 py-6 text-sm font-semibold text-slate-600">{t.dueDate}</td>
                                        <td className="px-8 py-6 text-right">
                                            <span className="inline-flex items-center space-x-2 px-4 py-1.5 bg-amber-50 text-amber-600 rounded-full text-[10px] font-black uppercase tracking-tighter border border-amber-100">
                                                <span className="w-1.5 h-1.5 bg-amber-500 rounded-full animate-pulse" />
                                                <span>Active</span>
                                            </span>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
};

export default Dashboard;
