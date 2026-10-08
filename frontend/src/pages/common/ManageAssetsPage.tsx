import { Monitor, PackageOpen } from 'lucide-react';

import StatusBadge from '@/components/StatusBadge';
import useGetAllAssets from '@/hooks/getAllAssets';
import useUpdateAsset from '@/hooks/useUpdateAsset';

import type { UserAsset } from '@/types';

type AssetStatus = UserAsset['status'];

const statusOptions: { value: AssetStatus; label: string }[] = [
    { value: 'Available', label: 'Available' },
    { value: 'Assigned', label: 'Assigned' },
    { value: 'In_repair', label: 'In Repair' },
    { value: 'Retired', label: 'Retired' },
];

const formatDate = (iso: string) => new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

const headings = ['Asset', 'Status', 'Assigned To', 'Last Updated', 'Change Status'];

function ManageAssetsPage() {
    const { data: allAssets, isPending } = useGetAllAssets();
    const { mutate: updateAsset, isPending: isUpdating, variables } = useUpdateAsset();

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

    const assets: UserAsset[] = allAssets?.data || [];

    const updatingId = isUpdating ? variables?.assetId : null;

    return (
        <div className='relative z-10 mx-auto max-w-6xl space-y-3'>
            <div className='flex items-center justify-between'>
                <h1 className='text-xs font-bold uppercase tracking-wider text-neutral-400'>All Assets</h1>
                <span className='text-xs font-medium text-neutral-400'>{assets.length} total</span>
            </div>

            <div className='overflow-hidden rounded-3xl border border-neutral-200 bg-background shadow-sm'>
                {assets.length === 0 ? (
                    <div className='flex flex-col items-center px-6 py-14 text-center'>
                        <div className='mb-4 rounded-full bg-neutral-100 p-4 text-neutral-400'>
                            <PackageOpen className='h-6 w-6' />
                        </div>
                        <p className='text-sm font-bold uppercase tracking-wider text-neutral-900'>No assets found</p>
                        <p className='mt-1 text-xs text-neutral-500'>Assets will appear here once they are added.</p>
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
                                {assets.map((asset) => {
                                    const rowBusy = updatingId === asset.id;

                                    return (
                                        <tr key={asset.id} className='transition-colors hover:bg-neutral-50'>
                                            <td className='px-5 py-4'>
                                                <div className='flex items-center gap-3'>
                                                    <div className='rounded-xl bg-neutral-100 p-2.5 text-primary'>
                                                        <Monitor className='h-4 w-4' />
                                                    </div>
                                                    <div className='min-w-0'>
                                                        <p className='max-w-48 truncate text-sm font-bold text-neutral-900'>{asset.name}</p>
                                                        <p className='font-mono text-xs text-neutral-500'>{asset.serialNumber}</p>
                                                    </div>
                                                </div>
                                            </td>

                                            <td className='px-5 py-4'>
                                                <StatusBadge status={asset.status} />
                                            </td>

                                            <td className='px-5 py-4'>
                                                {asset.assignedTo ? (
                                                    <>
                                                        <p className='max-w-48 truncate text-sm text-neutral-900'>{asset.assignedTo.email}</p>
                                                        <p className='text-xs text-neutral-500'>{asset.assignedTo.department}</p>
                                                    </>
                                                ) : (
                                                    <span className='text-sm text-neutral-400'>Unassigned</span>
                                                )}
                                            </td>

                                            <td className='whitespace-nowrap px-5 py-4 text-sm text-neutral-500'>
                                                {formatDate(asset.updatedAt)}
                                            </td>

                                            <td className='px-5 py-4'>
                                                <select aria-label={`Change status of ${asset.name}`} value={asset.status} disabled={rowBusy} onChange={(e) => updateAsset({ assetId: asset.id, status: e.target.value as AssetStatus })} className='w-36 cursor-pointer rounded-xl border border-neutral-200 bg-background px-3 py-2 text-xs font-bold uppercase tracking-wider text-neutral-700 transition-all focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary/80 disabled:cursor-not-allowed disabled:opacity-60'>
                                                    {statusOptions.map((opt) => (
                                                        <option key={opt.value} value={opt.value} disabled={opt.value === 'Assigned' && asset.status !== 'Assigned'}>
                                                            {opt.label}
                                                        </option>
                                                    ))}
                                                </select>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

export default ManageAssetsPage;