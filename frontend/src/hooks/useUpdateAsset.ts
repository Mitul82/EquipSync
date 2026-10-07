import toast from 'react-hot-toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes, UserAsset } from '@/types';

type AssetData = {
    assetId: string,
    status: 'Available' | 'Assigned' | 'In_repair' | 'Retired'
}

function useUpdateAsset() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (assetData: AssetData) => {
            const { data }: { data: ApiRes<UserAsset> } = await api.patch(`/asset/update/${assetData.assetId}?status=${assetData.status}`);

            return data;
        },
        onMutate: () => {
            toast.loading('Updating asset...', { id: 'updateAsset-toast' });
        },
        onSuccess: (data: ApiRes<UserAsset>) => {
            toast.success(data.message || 'Asset updated', { id: 'updateAsset-toast' });

            queryClient.invalidateQueries({ queryKey: ['getAllAssets'] });
            queryClient.invalidateQueries({ queryKey: ['getAllAvailableAssets'] });
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data?.message || 'Error in updating the asset';

            toast.error(errMessage, { id: 'updateAsset-toast' });
        }
    });
}

export default useUpdateAsset;