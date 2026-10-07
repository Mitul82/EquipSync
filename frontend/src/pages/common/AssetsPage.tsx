import { ClipboardList } from 'lucide-react';

import StatusBadge from '@/components/StatusBadge';
import useGetAllAssets from '@/hooks/getAllAssets';

function AssetsPage() {
    const { data: assets, isPending } = useGetAllAssets();

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

    const assetList = assets?.data || [];

    const headings = ['Name', 'Serial Number', 'Status', 'Assigned To']

    return (
        <div className='mx-auto my-10 max-w-6xl space-y-8 relative z-10'>
            <section>
                <div className='mb-3 flex items-center justify-between'>
                    <h2 className='text-xs font-bold uppercase tracking-wider text-primary'>My Requests</h2>
                    <span className='text-xs font-medium text-primary'>{assetList.length} total</span>
                </div>

                <div className='overflow-hidden rounded-3xl border border-primary bg-background shadow-sm'>
                    {assetList.length === 0 ? (
                        <div className='flex flex-col items-center px-6 py-14 text-center'>
                            <div className='mb-4 rounded-full bg-neutral-100 p-4 text-primary'>
                                <ClipboardList className='h-6 w-6' />
                            </div>
                            <p className='text-sm font-bold uppercase tracking-wider text-primary'>No assets created yet</p>
                            <p className='mt-1 text-xs text-primary'>Ask your admin to create assets to raise requests</p>
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
                                    {assetList.map((a) => (
                                        <tr key={a.id} className='transition-colors hover:bg-neutral-50'>
                                            <td className='max-w-xs px-5 py-4'>
                                                <p className='line-clamp-2 text-sm font-medium text-primary' title={a.name}>
                                                    {a.name}
                                                </p>
                                            </td>
                                            <td className='max-w-xs px-5 py-4'>
                                                <p className='line-clamp-2 text-sm font-medium text-primary' title={a.serialNumber}>
                                                    {a.serialNumber}
                                                </p>
                                            </td>
                                            <td className='px-5 py-4'>
                                                <StatusBadge status={a.status} />
                                            </td>
                                            <td className='max-w-xs px-5 py-4'>
                                                <p className='line-clamp-2 text-sm font-medium text-primary' title={a?.assignedTo?.email}>
                                                    {a?.assignedTo?.email || 'Not yet assigned'}
                                                </p>
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

export default AssetsPage;