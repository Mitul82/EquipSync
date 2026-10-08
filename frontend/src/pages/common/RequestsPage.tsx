import React from 'react';
import { ClipboardCheck } from 'lucide-react';

import useGetPendingRequests from '@/hooks/getPendingRequests';
import useReviewRequest from '@/hooks/useReviewRequest';
import StatusBadge from '@/components/StatusBadge';
import ReviewRequestModal from '@/components/ReviewRequestModal';
import type { UserRequests } from '@/types';

type Request = UserRequests[number];

const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

const headings = ['Requester', 'Description', 'Status', 'Requested On', ''];

function RequestsPage() {
    const { data: pendingRequests, isPending } = useGetPendingRequests();
    const { mutate: reviewRequest, isPending: isReviewing } = useReviewRequest();

    const [selected, setSelected] = React.useState<Request | null>(null);

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

    const requests: Request[] = pendingRequests?.data || [];

    const handleApprove = (assetId: string) => {
        if (!selected) return;
        reviewRequest({ id: selected.id, action: 'approve', assetId }, { onSuccess: () => setSelected(null) });
    }
    
    const handleDeny = () => {
        if (!selected) return;
        reviewRequest({ id: selected.id, action: 'deny' }, { onSuccess: () => setSelected(null) });
    }

    return (
        <div className='relative z-10 mx-auto max-w-6xl space-y-3'>
            <div className='flex items-center justify-between'>
                <h1 className='text-xs font-bold uppercase tracking-wider text-neutral-400'>Pending Requests</h1>
                <span className='text-xs font-medium text-neutral-400'>{requests.length} total</span>
            </div>

            <div className='overflow-hidden rounded-3xl border border-neutral-200 bg-background shadow-sm'>
                {requests.length === 0 ? (
                    <div className='flex flex-col items-center px-6 py-14 text-center'>
                        <div className='mb-4 rounded-full bg-neutral-100 p-4 text-neutral-400'>
                            <ClipboardCheck className='h-6 w-6' />
                        </div>
                        <p className='text-sm font-bold uppercase tracking-wider text-neutral-900'>All caught up</p>
                        <p className='mt-1 text-xs text-neutral-500'>There are no pending requests to review.</p>
                    </div>
                ) : (
                    <div className='overflow-x-auto'>
                        <table className='w-full min-w-175 text-left'>
                            <thead>
                                <tr className='border-b border-neutral-200 bg-neutral-50'>
                                    {headings.map((h, i) => (
                                        <th key={i} className='px-5 py-3.5 text-xs font-bold uppercase tracking-wider text-neutral-400'>
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className='divide-y divide-neutral-200'>
                                {requests.map((req) => (
                                    <tr key={req.id} className='transition-colors hover:bg-neutral-50'>
                                        <td className='px-5 py-4'>
                                            <p className='max-w-48 truncate text-sm font-medium text-neutral-900'>{req.requester.email}</p>
                                            <p className='text-xs text-neutral-500'>{req.requester.department}</p>
                                        </td>
                                        <td className='max-w-xs px-5 py-4'>
                                            <p className='line-clamp-2 text-sm text-neutral-700' title={req.description}>
                                                {req.description}
                                            </p>
                                        </td>
                                        <td className='px-5 py-4'>
                                            <StatusBadge status={req.status} />
                                        </td>
                                        <td className='whitespace-nowrap px-5 py-4 text-sm text-neutral-500'>
                                            {formatDate(req.createdAt)}
                                        </td>
                                        <td className='px-5 py-4 text-right'>
                                            <button type='button' onClick={() => setSelected(req)} className='cursor-pointer rounded-full bg-primary px-4 py-2 text-xs font-bold uppercase tracking-widest text-background shadow-md transition-all hover:bg-primary/90 hover:shadow-lg'>
                                                Review
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            <ReviewRequestModal key={selected?.id ?? 'closed'} request={selected} isSubmitting={isReviewing} onClose={() => setSelected(null)} onApprove={handleApprove} onDeny={handleDeny}/>
        </div>
    );
}

export default RequestsPage;