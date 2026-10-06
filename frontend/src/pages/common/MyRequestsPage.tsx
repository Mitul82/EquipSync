import { ClipboardList } from 'lucide-react';

import useGetUserRequests from '@/hooks/getUserRequests';
import StatusBadge from '@/components/common/StatusBadge';

const formatDate = (iso: string) => {
    return new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function MyRequestsPage() {
    const { data: userRequests, isPending } = useGetUserRequests();

    if(isPending) {
        return (
            <div className='flex min-h-[60vh] items-center justify-center'>
                <span className='relative flex h-4 w-4'>
                    <span className='absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75' />
                    <span className='relative inline-flex h-4 w-4 rounded-full bg-primary' />
                </span>
            </div>
        );
    }

    const requests = userRequests?.data || [];

    const headings = ['Request', 'Status', 'Reviewed By', 'Asset', 'Requested On']

    return (
        <div className='mx-auto my-10 max-w-6xl space-y-8 relative z-10'>
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

export default MyRequestsPage;