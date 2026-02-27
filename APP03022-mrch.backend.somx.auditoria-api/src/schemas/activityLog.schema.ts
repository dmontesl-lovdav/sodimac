import {  z } from "zod/v4";

export const CreateActivityLogSchema = z.object({
    traceId: z.string(),
    traceFrontId: z.string().optional(),
    modulo: z.string(),
    action: z.string(),
    serviceName: z.string(),
    userId: z.string(),
    isError: z.boolean(),
    message: z.string(),
    messageDetail: z.string(),
    details: z.any().optional(),
    durationms: z.number().optional(),
    timestamp: z.coerce.date()
});

export const ListActivityLogQuerySchema = z.object({
    dateAtInitial: z.coerce.date(),
    dateAtEnd: z.coerce.date(),
    traceId: z.string().optional(),
    modulo: z.string().optional(),
    serviceName: z.string().optional(),
    pageNumber: z.number().int(),
    pageSize: z.number().int(),
})  .superRefine((data, ctx) => {
    // The `superRefine` callback receives the validated object data and a context object (ctx)

    // Check if the end date is before the start date
    if (data.dateAtEnd < data.dateAtInitial) {
      // If invalid, add a custom issue to the context
        ctx.issues.push({
            message: "End date cannot be earlier than start date.", // Your custom error message
            path: ["endDated"],
            input: undefined,
            code: "custom"
        });
        return;
    }

    let startDate = new Date(data.dateAtInitial);
    let endDate = new Date(data.dateAtEnd);

       // Optional: Reset time components to midnight local time for consistent day calculation
    // This step can be adjusted based on timezone requirements.
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(0, 0, 0, 0);

    const millisecondsPerDay = 1000 * 60 * 60 * 24;
    const differenceInMilliseconds = endDate.getTime() - startDate.getTime();
    const differenceInDays = Math.round(differenceInMilliseconds / millisecondsPerDay);

    if (differenceInDays > 30) {
        ctx.issues.push({
            message: "The date range must be less than or equal to 30 days", // Your custom error message
            path: ["dateAtInitial, dateAtEnd"],
            input: undefined,
            code: "custom"
        });
        return;
    }

  });


export type CreateActivityLogDto = z.infer<typeof CreateActivityLogSchema>;
export type ListActivityLogQueryDto = z.infer<typeof ListActivityLogQuerySchema>;