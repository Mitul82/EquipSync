import React from 'react';
import { Search, Users } from 'lucide-react';

import useAuth from '@/hooks/useAuth';
import useGetAllUsers from '@/hooks/getAllUsers';
import useUpdateUser from '@/hooks/useUpdateUser';

import type { AuthUser } from '@/types';

type Role = AuthUser['role'];

const roleStyles: Record<Role, string> = {
    Admin: 'bg-red-50 text-red-700 border-red-200',
    Manager: 'bg-blue-50 text-blue-700 border-blue-200',
    Employee: 'bg-neutral-100 text-neutral-600 border-neutral-200',
};

const roleOptions: ('All' | Role)[] = ['All', 'Admin', 'Manager', 'Employee'];

function UsersPage() {
    const { data: currentUser } = useAuth();
    const { data: allUsers, isPending } = useGetAllUsers();
    const { mutate: updateUser, isPending: isUpdating, variables } = useUpdateUser();

    const [search, setSearch] = React.useState<string>('');
    const [roleFilter, setRoleFilter] = React.useState<'All' | Role>('All');

    const users: AuthUser[] = React.useMemo(() => allUsers?.data || [], [allUsers]);

    const filtered = React.useMemo(() => {
        const q = search.trim().toLowerCase();

        return users.filter((u) => {
            const matchesRole = roleFilter === 'All' || u.role === roleFilter;
            const matchesSearch = !q || u.email.toLowerCase().includes(q) || u.department.toLowerCase().includes(q);

            return matchesRole && matchesSearch;
        });
    }, [users, search, roleFilter]);

    if (isPending) {
        return (
            <div className='flex min-h-[60vh] items-center justify-center'>
                <span className='relative flex h-4 w-4'>
                    <span className='absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75' />
                    <span className='relative inline-flex h-4 w-4 rounded-full bg-primary' />
                </span>
            </div>
        );
    }

    const updatingId = isUpdating ? variables?.userId : null;

    const headings = ['User', 'Department', 'Role', 'Change Role']

    return (
        <div className='relative z-10 mx-auto max-w-6xl space-y-3'>
            <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
                <div className='flex items-center gap-3'>
                    <h1 className='text-xs font-bold uppercase tracking-wider text-neutral-400'>All Users</h1>
                    <span className='text-xs font-medium text-neutral-400'>
                        {filtered.length === users.length ? `${users.length} total` : `${filtered.length} of ${users.length}`}
                    </span>
                </div>

                <div className='flex flex-col gap-3 sm:flex-row'>
                    <div className='relative'>
                        <div className='pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-neutral-400'>
                            <Search className='h-4 w-4' />
                        </div>
                        <input type='text' value={search} onChange={(e) => setSearch(e.target.value)} placeholder='Search email or department' aria-label='Search users' className='w-full rounded-xl border border-neutral-200 bg-background py-2.5 pl-10 pr-4 text-sm text-neutral-900 placeholder:text-neutral-400 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80 sm:w-64'/>
                    </div>

                    <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value as 'All' | Role)} aria-label='Filter by role' className='cursor-pointer rounded-xl border border-neutral-200 bg-background px-4 py-2.5 text-xs font-bold uppercase tracking-wider text-neutral-700 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80'>
                        {roleOptions.map((r) => (
                            <option key={r} value={r}>{r === 'All' ? 'All Roles' : r}</option>
                        ))}
                    </select>
                </div>
            </div>

            <div className='overflow-hidden rounded-3xl border border-neutral-200 bg-background shadow-sm'>
                {filtered.length === 0 ? (
                    <div className='flex flex-col items-center px-6 py-14 text-center'>
                        <div className='mb-4 rounded-full bg-neutral-100 p-4 text-neutral-400'>
                            <Users className='h-6 w-6' />
                        </div>
                        <p className='text-sm font-bold uppercase tracking-wider text-neutral-900'>No users found</p>
                        <p className='mt-1 text-xs text-neutral-500'>
                            {users.length === 0 ? 'Users will appear here once they sign up.' : 'Try a different search or role filter.'}
                        </p>
                    </div>
                ) : (
                    <div className='overflow-x-auto'>
                        <table className='w-full min-w-175 text-left'>
                            <thead>
                                <tr className='border-b border-neutral-200 bg-neutral-50'>
                                    {headings.map((h) => (
                                        <th key={h} className='px-5 py-3.5 text-xs font-bold uppercase tracking-wider text-neutral-400'>
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className='divide-y divide-neutral-200'>
                                {filtered.map((user) => (
                                    <tr key={user.id} className='transition-colors hover:bg-neutral-50'>
                                        <td className='px-5 py-4'>
                                            <div className='flex items-center gap-3'>
                                                <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-neutral-100 text-xs font-bold uppercase text-primary'>
                                                    {user.email.charAt(0)}
                                                </div>
                                                <p className='max-w-64 truncate text-sm font-medium text-neutral-900'>
                                                    {user.email}
                                                    {user.id === currentUser?.id && (
                                                        <span className='ml-2 text-xs font-bold uppercase tracking-wider text-neutral-400'>You</span>
                                                    )}
                                                </p>
                                            </div>
                                        </td>
                                        <td className='px-5 py-4 text-sm text-neutral-600'>{user.department}</td>
                                        <td className='px-5 py-4'>
                                            <span className={`inline-flex rounded-full border px-3 py-1 text-xs font-bold uppercase tracking-wider ${roleStyles[user.role]}`}>
                                                {user.role}
                                            </span>
                                        </td>
                                        <td className='px-5 py-4'>
                                            <select aria-label={`Change role of ${user.email}`} value={user.role} disabled={updatingId === user.id || user.id === currentUser?.id} title={user.id === currentUser?.id ? 'You cannot change your own role' : undefined} onChange={(e) => updateUser({ userId: user.id, role: e.target.value as Role })} className='w-36 cursor-pointer rounded-xl border border-neutral-200 bg-background px-3 py-2 text-xs font-bold uppercase tracking-wider text-neutral-700 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80 disabled:cursor-not-allowed disabled:opacity-60'>
                                                {(['Admin', 'Manager', 'Employee'] as Role[]).map((r) => (
                                                    <option key={r} value={r}>{r}</option>
                                                ))}
                                            </select>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

export default UsersPage;