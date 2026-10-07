import toast from 'react-hot-toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes, EquipmentRequest } from '@/types';

type ReviewVars = { id: string, action: 'approve', assetId: string } | { id: string, action: 'deny' }

function useReviewRequest() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (vars: ReviewVars) => {
            if(vars.action === 'approve') {
                const { data }: { data: ApiRes<EquipmentRequest> } = await api.patch(`/equipment/request/approve/${vars.id}`, { assetId: vars.assetId, status: 'Approved' });

                return data;
            }

            const { data }: { data: ApiRes<EquipmentRequest> } = await api.patch(`/equipment/request/deny/${vars.id}`);

            return data;
        },
        onSuccess: (_data, vars) => {
            toast.success(vars.action === 'approve' ? 'Request approved' : 'Request denied', { id: 'reviewRequest-toast' });

            queryClient.invalidateQueries({ queryKey: ['getPendingRequests'] });
            queryClient.invalidateQueries({ queryKey: ['getAllAvailableAssets'] });
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data?.message || 'Could not update the request. Please try again.';

            toast.error(errMessage, { id: 'reviewRequest-toast' });
        }
    });
}

export default useReviewRequest;