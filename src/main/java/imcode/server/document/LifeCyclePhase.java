package imcode.server.document;

import imcode.server.document.index.DocumentIndex;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TermQuery;

import com.imcode.imcms.api.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.util.DateUtil;

public abstract class LifeCyclePhase {

    public static final LifeCyclePhase NEW = new LifeCyclePhase("new") {
        public Query asQuery(Date time) {
            return getStatusQuery(Document.PublicationStatus.NEW);
        }
    };
    public static final LifeCyclePhase DISAPPROVED = new LifeCyclePhase("disapproved") {
        public Query asQuery(Date time) {
            return getStatusQuery(Document.PublicationStatus.DISAPPROVED);
        }
    };
    public static final LifeCyclePhase UNPUBLISHED = new LifeCyclePhase("unpublished") {
        public Query asQuery(Date time) {
            return add(getApprovedBooleanQuery(), getPublicationEndRangeQuery(time));
        }
    };
    public static final LifeCyclePhase APPROVED = new LifeCyclePhase("approved") {
        public Query asQuery(Date time) {
            return subtract(getApprovedNonUnpublishedQuery(time), getPublicationStartRangeQuery(time));
        }
    };
    public static final LifeCyclePhase ARCHIVED = new LifeCyclePhase("archived") {
        public Query asQuery(Date time) {
            return add(getPublishedQuery(time), getArchivedRangeQuery(time));
        }
    };
    public static final LifeCyclePhase PUBLISHED = new LifeCyclePhase("published") {
        public Query asQuery(Date time) {
            return subtract(getPublishedQuery(time), getArchivedRangeQuery(time));
        }
    };

    public static final LifeCyclePhase[] ALL = new LifeCyclePhase[]{
            NEW, DISAPPROVED, APPROVED,
            PUBLISHED, ARCHIVED, UNPUBLISHED
    };

    private static BooleanQuery add(BooleanQuery query, Query otherQuery) {
        query.add(otherQuery, BooleanClause.Occur.MUST);
        return query;
    }

    private static BooleanQuery subtract(BooleanQuery minuend, Query subtrahend) {
        minuend.add(subtrahend, BooleanClause.Occur.MUST_NOT);

        return minuend;
    }

    private static TermRangeQuery getPublicationStartRangeQuery(Date now) {
        return getDateRangeQuery(DocumentIndex.FIELD__PUBLICATION_START_DATETIME, now);
    }

    private static TermRangeQuery getPublicationEndRangeQuery(Date now) {
        return getDateRangeQuery(DocumentIndex.FIELD__PUBLICATION_END_DATETIME, now);
    }

    private static TermRangeQuery getArchivedRangeQuery(Date now) {
        return getDateRangeQuery(DocumentIndex.FIELD__ARCHIVED_DATETIME, now);
    }


    /**
     * @param field range field name.
     * @param now   range upper bound.
     * @return a TermRangeQuery in an interval from infinity to now.
     */
    private static TermRangeQuery getDateRangeQuery(String field, Date now) {
        return new TermRangeQuery(field,
                null,
                new BytesRef(DateUtil.getThreadLocalDateFormat().format(now)),
                true, true);
    }

    private static BooleanQuery getPublishedQuery(Date now) {
        return add(getApprovedNonUnpublishedQuery(now), getPublicationStartRangeQuery(now));
    }

    private static BooleanQuery getApprovedNonUnpublishedQuery(Date now) {
        return subtract(getApprovedBooleanQuery(), getPublicationEndRangeQuery(now));
    }

    private static BooleanQuery getApprovedBooleanQuery() {
        return add(new BooleanQuery(), getStatusQuery(Document.PublicationStatus.APPROVED));
    }

    private static TermQuery getStatusQuery(Document.PublicationStatus publicationStatus) {
        return new TermQuery(new Term(DocumentIndex.FIELD__STATUS, publicationStatus.toString()));
    }


    private final String name;

    private LifeCyclePhase(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public abstract Query asQuery(Date time);

}
