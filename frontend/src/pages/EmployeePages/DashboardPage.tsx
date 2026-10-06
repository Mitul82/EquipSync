import { Monitor, Calendar, Hash, PackageOpen, ClipboardList } from 'lucide-react';

import useGetUserAsset from '@/hooks/getUserAsset';
import useGetUserRequests from '@/hooks/getUserRequests';
import StatusBadge from '@/components/common/StatusBadge';

const formatDate = (iso: string) => {
    return new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function DashboardPage() {
    const { data: userAsset, isPending } = useGetUserAsset();
    const { data: userRequests, isPending: isLoading } = useGetUserRequests();

    if (isPending || isLoading) {
        return (
            <div className='flex min-h-[60vh] items-center justify-center'>
                <span className='relative flex h-4 w-4'>
                    <span className='absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75' />
                    <span className='relative inline-flex h-4 w-4 rounded-full bg-primary' />
                </span>
            </div>
        );
    }

    const asset = userAsset?.data;
    const requests = userRequests?.data || [];

    const headings = ['Request', 'Status', 'Reviewed By', 'Asset', 'Requested On']

    return (
        <div className='mx-auto max-w-6xl space-y-8 relative z-10'>
            <section>
                <h2 className='mb-3 text-xs font-bold uppercase tracking-wider text-priamry'>My Asset</h2>

                {asset ? (
                    <div className='rounded-3xl border border-primary bg-background p-5 shadow-sm sm:p-6'>
                        <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
                            <div className='flex min-w-0 items-center gap-4'>
                                <div className='rounded-2xl bg-primary p-3.5 text-background shadow-md'>
                                    <Monitor className='h-6 w-6' />
                                </div>
                                <div className='min-w-0'>
                                    <h3 className='truncate text-base font-bold text-primary'>{asset.name}</h3>
                                    <p className='mt-0.5 truncate font-mono text-xs text-primary'>S/N {asset.serialNumber}</p>
                                </div>
                            </div>
                            <StatusBadge status={asset.status} />
                        </div>

                        <div className='mt-5 grid gap-4 border-t border-primary pt-5 sm:grid-cols-3'>
                            <div>
                                <p className='mb-1.5 text-xs font-bold uppercase tracking-wider text-primary'>Asset ID</p>
                                <p className='inline-flex items-center gap-1.5 font-mono text-sm text-primary'>
                                    <Hash className='h-3.5 w-3.5 text-primary' />
                                    <span className='truncate'>{asset.id}</span>
                                </p>
                            </div>
                            <div>
                                <p className='mb-1.5 text-xs font-bold uppercase tracking-wider text-primary'>Added On</p>
                                <p className='inline-flex items-center gap-1.5 text-sm text-neutral-900'>
                                    <Calendar className='h-3.5 w-3.5 text-neutral-400' />
                                    {formatDate(asset.createdAt)}
                                </p>
                            </div>
                            <div>
                                <p className='mb-1.5 text-xs font-bold uppercase tracking-wider text-primary'>Last Updated</p>
                                <p className='inline-flex items-center gap-1.5 text-sm text-primary'>
                                    <Calendar className='h-3.5 w-3.5 text-primary' />
                                    {formatDate(asset.updatedAt)}
                                </p>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className='flex items-center gap-4 rounded-3xl border border-dashed border-primary bg-background p-6'>
                        <div className='rounded-full bg-neutral-100 p-3 text-primary'>
                            <PackageOpen className='h-5 w-5' />
                        </div>
                        <div>
                            <p className='text-sm font-bold uppercase tracking-wider text-primary'>No asset assigned</p>
                            <p className='mt-0.5 text-xs text-primary'>Raise a request and it will show up here once fulfilled.</p>
                        </div>
                    </div>
                )}
            </section>

            <section>
                <div className='mb-3 flex items-center justify-between'>
                    <h2 className='text-xs font-bold uppercase tracking-wider text-primary'>My Requests</h2>
                    <span className='text-xs font-medium text-primary'>{requests.length} total</span>
                </div>

                <div className='overflow-hidden rounded-3xl border border-primary bg-background shadow-sm'>
                    {requests.length === 0 ? (
                        <div className='flex flex-col items-center px-6 py-14 text-center'>
                            <div className='mb-4 rounded-full bg-neutral-100 p-4 text-primary'>
                                <ClipboardList className='h-6 w-6' />
                            </div>
                            <p className='text-sm font-bold uppercase tracking-wider text-primary'>No requests yet</p>
                            <p className='mt-1 text-xs text-primary'>Requests you raise will show up here.</p>
                        </div>
                    ) : (
                        <div className='overflow-x-auto'>
                            <table className='w-full min-w-175 text-left'>
                                <thead>
                                    <tr className='border-b border-primary bg-neutral-50'>
                                        {headings.map((h) => (
                                            <th key={h} className='px-5 py-3.5 text-xs font-bold uppercase tracking-wider text-primary'>
                                                {h}
                                            </th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody className='divide-y divide-neutral-200'>
                                    {requests.map((req) => (
                                        <tr key={req.id} className='transition-colors hover:bg-neutral-50'>
                                            <td className='max-w-xs px-5 py-4'>
                                                <p className='line-clamp-2 text-sm font-medium text-primary' title={req.description}>
                                                    {req.description}
                                                </p>
                                            </td>
                                            <td className='px-5 py-4'>
                                                <StatusBadge status={req.status} />
                                            </td>
                                            <td className='px-5 py-4'>
                                                {req.reviewedBy ? (
                                                    <>
                                                        <p className='max-w-48 truncate text-sm text-primary'>{req.reviewedBy.email}</p>
                                                        <p className='text-xs text-primary'>{req.reviewedBy.role}</p>
                                                    </>
                                                ) : (
                                                    <span className='text-sm text-primary'>Awaiting review</span>
                                                )}
                                            </td>
                                            <td className='px-5 py-4'>
                                                {req.assignedAsset ? (
                                                    <>
                                                        <p className='max-w-48 truncate text-sm font-medium text-primary'>{req.assignedAsset.name}</p>
                                                        <p className='font-mono text-xs text-primary'>{req.assignedAsset.serialNumber}</p>
                                                    </>
                                                ) : (
                                                    <span className='text-sm text-primary'>Not assigned</span>
                                                )}
                                            </td>
                                            <td className='whitespace-nowrap px-5 py-4 text-sm text-primary'>
                                                {formatDate(req.createdAt)}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}

export default DashboardPage;