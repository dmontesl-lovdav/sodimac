import { translateDate, translateHour, byCreationTimeSorter } from "../components/RequestUtils";

export default function CommentSummary({ comments }) {
    function renderComments() {
        if (!comments || comments.length === 0) {
            return <div>--</div>;
        }

        return comments
            .sort(byCreationTimeSorter)
            .map((comment, i) => (
                <div key={comment.id || i}>
                    <div className="mt-1">{comment.comment}</div>
                    <div className="text-xs text-stone-600/50">
                        {comment.author} {translateDate(comment.creationTime)} {translateHour(comment.creationTime)}
                    </div>
                </div>
            ));
    }

    return (
        <div>
            <span className="font-bold">Observaciones:</span>
            {renderComments()}
        </div>
    );
}
